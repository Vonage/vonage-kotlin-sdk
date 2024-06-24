package com.vonage.client.kt

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.*
import com.vonage.client.VonageApiResponseException
import com.vonage.client.common.HttpMethod
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.util.*
import kotlin.test.assertEquals

abstract class AbstractTest {
    protected val apiKey = "a1b2c3d4"
    protected val applicationId = "00000000-0000-4000-8000-000000000000"
    private val apiSecret = "1234567890abcdef"
    private val apiKeySecretEncoded = "YTFiMmMzZDQ6MTIzNDU2Nzg5MGFiY2RlZg=="
    protected val toNumber = "447712345689"
    protected val altNumber = "447700900001"
    protected val testUuid = UUID.fromString("aaaaaaaa-bbbb-4ccc-8ddd-0123456789ab")

    private val port = 8081
    val wiremock: WireMockServer = WireMockServer(
        options().port(port).notifier(ConsoleNotifier(false))
    )

    val vonageClient = Vonage {
        apiKey(apiKey); apiSecret(apiSecret); applicationId(applicationId)
        privateKeyPath("src/test/resources/com/vonage/client/kt/application_key")
        httpConfig {
            baseUri("http://localhost:$port")
        }
    }

    @BeforeEach
    fun setUp() {
        wiremock.start()
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }

    protected enum class ContentType(val mime: String) {
        APPLICATION_JSON("application/json"),
        FORM_URLENCODED("application/x-www-form-urlencoded");

        @Override
        override fun toString(): String {
            return mime
        }
    }

    protected enum class AuthType {
        JWT, API_KEY_SECRET_HEADER, API_KEY_SECRET_QUERY_PARAMS
    }

    protected fun mockRequest(
        httpMethod: HttpMethod,
        expectedUrl: String,
        contentType: ContentType? = null,
        accept: ContentType? = null,
        authType: AuthType? = null,
        expectedBodyParams: Map<String, Any>? = null): BuildingStep =
            wiremock.requestServerBuilderStep({
                url equalTo expectedUrl
                headers contains "User-Agent" like "vonage-java-sdk\\/.+ java\\/.+"
                if (authType != null) {
                    val authHeaderName = "Authorization"
                    when (authType) {
                        AuthType.JWT -> headers contains authHeaderName like
                                    "Bearer eyJ0eXBlIjoiSldUIiwiYWxnIjoiUlMyNTYifQ(\\..+){2}"
                        AuthType.API_KEY_SECRET_HEADER ->
                            headers contains authHeaderName equalTo "Basic $apiKeySecretEncoded"
                        AuthType.API_KEY_SECRET_QUERY_PARAMS -> {
                            headers contains "api_key" equalTo apiKey
                            headers contains "api_secret" equalTo apiSecret
                        }
                    }
                }
                if (contentType != null) {
                    headers contains "Content-Type" equalTo contentType.mime
                }
                if (accept != null) {
                    headers contains "Accept" equalTo accept.mime
                }
                if (expectedBodyParams != null) {
                    body equalTo ObjectMapper().writeValueAsString(expectedBodyParams)
                }
            }, when (httpMethod) {
                    HttpMethod.GET -> WireMock::get
                    HttpMethod.POST -> WireMock::post
                    HttpMethod.PUT -> WireMock::put
                    HttpMethod.PATCH -> WireMock::patch
                    HttpMethod.DELETE -> WireMock::delete
                    else -> throw IllegalArgumentException("Unhandled HTTP method: $httpMethod")
            })

    protected fun mockJsonJwtPost(expectedUrl: String,
                                  expectedRequestParams: Map<String, Any>? = null,
                                  status: Int = 200,
                                  expectedResponseParams: Map<String, Any>? = null) =

        mockRequest(HttpMethod.POST, expectedUrl,
            contentType = if (expectedRequestParams != null) ContentType.APPLICATION_JSON else null,
            accept = if (expectedResponseParams != null) ContentType.APPLICATION_JSON else null,
            AuthType.JWT, expectedRequestParams
        ).mockReturn(status, expectedResponseParams)

    protected fun mockDelete(expectedUrl: String, authType: AuthType? = null): ReturnsStep =
        mockRequest(HttpMethod.DELETE, expectedUrl, authType = authType).mockReturn(204)

    protected fun BuildingStep.mockReturn(
            status: Int? = null, expectedBody: Map<String, Any>? = null): ReturnsStep =
        returns {
            statusCode = if
                    (status == null && expectedBody == null) 204
                    else status ?: 200

            if (expectedBody != null) {
                body = ObjectMapper().writeValueAsString(expectedBody)
                header = "Content-Type" to ContentType.APPLICATION_JSON.mime
            }
        }

    protected inline fun <reified E: VonageApiResponseException> assertApiResponseException(
            url: String, requestMethod: HttpMethod, actualCall: () -> Unit) {

        assert402ApiResponseException<E>(url, requestMethod, actualCall)
        assert429ApiResponseException<E>(url, requestMethod, actualCall)
    }

    protected inline fun <reified E: VonageApiResponseException> assertApiResponseException(
            url: String, requestMethod: HttpMethod, actualCall: () -> Unit, status: Int,
            errorType: String, title: String, detail: String, instance: String): E {

        mockRequest(requestMethod, url).mockReturn(status, mapOf(
            "type" to errorType, "title" to title,
            "detail" to detail, "instance" to instance
        ))

        val exception = assertThrows<E>(actualCall)

        assertEquals(status, exception.statusCode)
        assertEquals(URI.create(errorType), exception.type)
        assertEquals(title, exception.title)
        assertEquals(instance, exception.instance)
        assertEquals(detail, exception.detail)
        return exception
    }

    protected inline fun <reified E: VonageApiResponseException> assert402ApiResponseException(
            url: String, requestMethod: HttpMethod, actualCall: () -> Unit): E =

        assertApiResponseException(url, requestMethod, actualCall, 402,
            "https://developer.nexmo.com/api-errors/#low-balance",
            "Low balance",
            "This request could not be performed due to your account balance being low.",
            "bf0ca0bf927b3b52e3cb03217e1a1ddf"
        )

    protected inline fun <reified E: VonageApiResponseException> assert429ApiResponseException(
            url: String, requestMethod: HttpMethod, actualCall: () -> Unit): E =
        assertApiResponseException(url, requestMethod, actualCall, 429,
            "https://www.developer.vonage.com/api-errors#throttled",
            "Rate Limit Hit",
            "Please wait, then retry your request",
            "06032957-99ce-41ee-978b-9a390cd5a89b"
        )
}