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
    private val signatureSecret = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQR"
    private val apiKeySecretEncoded = "YTFiMmMzZDQ6MTIzNDU2Nzg5MGFiY2RlZg=="
    private val privateKeyPath = "src/test/resources/com/vonage/client/kt/application_key"
    protected val testUuid = UUID.fromString("aaaaaaaa-bbbb-4ccc-8ddd-0123456789ab")
    protected val toNumber = "447712345689"
    protected val altNumber = "447700900001"
    protected val text = "Hello, World!"
    protected val networkCode = "65512"
    protected val startTime = "2020-09-17T12:34:56Z"
    protected val endTime = "2021-09-17T12:35:28Z"
    protected val timestamp = "2016-11-14T07:45:14Z"

    private val port = 8081
    val wiremock: WireMockServer = WireMockServer(
        options().port(port).notifier(ConsoleNotifier(false))
    )

    val vonage = Vonage {
        apiKey(apiKey); apiSecret(apiSecret);
        signatureSecret(signatureSecret); applicationId(applicationId)
        privateKeyPath(privateKeyPath)
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
        expectedParams: Map<String, Any>? = null): BuildingStep =
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
                if (expectedParams != null) {
                    if (contentType == ContentType.APPLICATION_JSON) {
                        body equalTo ObjectMapper().writeValueAsString(expectedParams)
                    }
                    else {
                        url like "$expectedUrl\\?.+"
                        expectedParams.forEach { (k, v) ->
                            queryParams contains k equalTo v.toString()
                        }
                    }
                }
            }, when (httpMethod) {
                    HttpMethod.GET -> WireMock::get
                    HttpMethod.POST -> WireMock::post
                    HttpMethod.PUT -> WireMock::put
                    HttpMethod.PATCH -> WireMock::patch
                    HttpMethod.DELETE -> WireMock::delete
                    else -> throw IllegalArgumentException("Unhandled HTTP method: $httpMethod")
            })

    private fun mockP(requestMethod: HttpMethod, expectedUrl: String,
                      expectedRequestParams: Map<String, Any>? = null,
                      status: Int = 200, authType: AuthType? = AuthType.JWT,
                      expectedResponseParams: Map<String, Any>? = null) =

        mockRequest(requestMethod, expectedUrl,
            contentType = if (expectedRequestParams != null) ContentType.APPLICATION_JSON else null,
            accept = if (expectedResponseParams != null && status < 400) ContentType.APPLICATION_JSON else null,
            authType = authType, expectedRequestParams
        ).mockReturn(status, expectedResponseParams)

    protected fun mockPost(expectedUrl: String,
                           expectedRequestParams: Map<String, Any>? = null,
                           status: Int = 200,
                           authType: AuthType? = AuthType.JWT,
                           expectedResponseParams: Map<String, Any>? = null) =
        mockP(HttpMethod.POST, expectedUrl, expectedRequestParams, status, authType, expectedResponseParams)

    protected fun mockPut(expectedUrl: String,
                           expectedRequestParams: Map<String, Any>? = null,
                           status: Int = 200,
                           authType: AuthType? = AuthType.JWT,
                           expectedResponseParams: Map<String, Any>? = null) =
        mockP(HttpMethod.PUT, expectedUrl, expectedRequestParams, status, authType, expectedResponseParams)

    protected fun mockPatch(expectedUrl: String,
                          expectedRequestParams: Map<String, Any>? = null,
                          status: Int = 200,
                          authType: AuthType? = AuthType.JWT,
                          expectedResponseParams: Map<String, Any>? = null) =
        mockP(HttpMethod.PUT, expectedUrl, expectedRequestParams, status, authType, expectedResponseParams)

    protected fun mockDelete(expectedUrl: String, authType: AuthType? = null,
                             expectedResponseParams: Map<String, Any>? = null) =
        mockRequest(HttpMethod.DELETE, expectedUrl, authType = authType)
            .mockReturn(if (expectedResponseParams == null) 204 else 200, expectedResponseParams)

    protected fun mockGet(expectedUrl: String,
                             expectedQueryParams: Map<String, Any>? = null,
                             status: Int = 200,
                             authType: AuthType? = AuthType.JWT,
                             expectedResponseParams: Map<String, Any>) =

        mockRequest(HttpMethod.GET, expectedUrl, accept = ContentType.APPLICATION_JSON, authType = authType,
            expectedParams = expectedQueryParams).mockReturn(status, expectedResponseParams)


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
            url: String, requestMethod: HttpMethod, actualCall: () -> Any) {

        assert402ApiResponseException<E>(url, requestMethod, actualCall)
        assert429ApiResponseException<E>(url, requestMethod, actualCall)
    }

    protected inline fun <reified E: VonageApiResponseException> assertApiResponseException(
            url: String, requestMethod: HttpMethod, actualCall: () -> Any, status: Int,
            errorType: String, title: String, detail: String, instance: String): E {

        mockRequest(requestMethod, url).mockReturn(status, mapOf(
            "type" to errorType, "title" to title,
            "detail" to detail, "instance" to instance
        ))

        val exception = assertThrows<E> { actualCall.invoke() }

        assertEquals(status, exception.statusCode)
        assertEquals(URI.create(errorType), exception.type)
        assertEquals(title, exception.title)
        assertEquals(instance, exception.instance)
        assertEquals(detail, exception.detail)
        return exception
    }

    protected inline fun <reified E: VonageApiResponseException> assert402ApiResponseException(
            url: String, requestMethod: HttpMethod, actualCall: () -> Any): E =

        assertApiResponseException(url, requestMethod, actualCall, 402,
            "https://developer.nexmo.com/api-errors/#low-balance",
            "Low balance",
            "This request could not be performed due to your account balance being low.",
            "bf0ca0bf927b3b52e3cb03217e1a1ddf"
        )

    protected inline fun <reified E: VonageApiResponseException> assert429ApiResponseException(
            url: String, requestMethod: HttpMethod, actualCall: () -> Any): E =
        assertApiResponseException(url, requestMethod, actualCall, 429,
            "https://www.developer.vonage.com/api-errors#throttled",
            "Rate Limit Hit",
            "Please wait, then retry your request",
            "06032957-99ce-41ee-978b-9a390cd5a89b"
        )
}