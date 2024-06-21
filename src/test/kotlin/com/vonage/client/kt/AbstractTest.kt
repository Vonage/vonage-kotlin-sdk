package com.vonage.client.kt

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.*
import com.vonage.client.common.HttpMethod
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class AbstractTest {
    val apiKey = "a1b2c3d4"
    val applicationId = "00000000-0000-4000-8000-000000000000"
    private val apiSecret = "1234567890abcdef"
    private val apiKeySecretEncoded = "YTFiMmMzZDQ6MTIzNDU2Nzg5MGFiY2RlZg=="

    val port = 8081
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
        JWT, API_KEY_SECRET
    }

    protected fun baseMockRequest(
        httpMethod: HttpMethod,
        expectedUrl: String,
        contentType: ContentType? = null,
        accept: ContentType? = null,
        authType: AuthType? = null,
        expectedBodyParams: Map<String, Any>? = null) =
            wiremock.requestServerBuilderStep({
                url equalTo expectedUrl
                headers contains "User-Agent" like "vonage-java-sdk.*"
                if (authType != null) {
                    headers contains "Authorization" like when (authType) {
                        AuthType.JWT -> "Bearer eyJ.+"
                        AuthType.API_KEY_SECRET -> "Basic $apiKeySecretEncoded"
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
}