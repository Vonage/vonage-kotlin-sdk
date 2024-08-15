/*
 *   Copyright 2024 Vonage
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.vonage.client.kt

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.*
import com.vonage.client.VonageApiResponseException
import com.vonage.client.common.HttpMethod
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals

abstract class AbstractTest {
    protected val apiKey = "a1b2c3d4"
    protected val apiKey2 = "f9e8d7c6"
    protected val applicationId = "00000000-0000-4000-8000-000000000000"
    protected val accessToken = "abc123456def"
    private val apiSecret = "1234567890abcdef"
    private val apiKeySecretEncoded = "YTFiMmMzZDQ6MTIzNDU2Nzg5MGFiY2RlZg=="
    private val privateKeyPath = "src/test/resources/com/vonage/client/kt/application_key"
    private val signatureSecretName = "sig"
    private val apiSecretName = "api_secret"
    private val apiKeyName = "api_key"
    private val authHeaderName = "Authorization"
    private val basicSecretEncodedHeader = "Basic $apiKeySecretEncoded"
    private val jwtBearerPattern = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9(\\..+){2}"
    private val accessTokenBearer = "Bearer $accessToken"
    protected val testUuidStr = "aaaaaaaa-bbbb-4ccc-8ddd-0123456789ab"
    protected val testUuid: UUID = UUID.fromString(testUuidStr)
    protected val toNumber = "447712345689"
    protected val altNumber = "447700900001"
    protected val brand = "Nexmo KT"
    protected val text = "Hello, World!"
    protected val country = "GB"
    protected val secret = "ABCDEFGH01234abc"
    protected val sipUri = "sip:rebekka@sip.example.com"
    protected val clientRef = "my-personal-reference"
    protected val textHexEncoded = "48656c6c6f2c20576f726c6421"
    protected val entityId = "1101407360000017170"
    protected val contentId = "1107158078772563946"
    protected val smsMessageId = "0C000000217B7F02"
    protected val callIdStr = "63f61863-4a51-4f6b-86e1-46edebcf9356"
    protected val networkCode = "65512"
    protected val startTimeStr = "2020-09-17T12:34:56Z"
    protected val startTime: Instant = Instant.parse(startTimeStr)
    protected val endTimeStr = "2021-09-17T12:35:28Z"
    protected val endTime: Instant = Instant.parse(endTimeStr)
    protected val timestampStr = "2016-11-14T07:45:14Z"
    protected val timestampDateStr = "2016-11-14 07:45:14"
    protected val timestampDate = strToDate(timestampDateStr)
    protected val timestampDate2Str = "2019-03-02 18:46:57"
    protected val timestampDate2 = strToDate(timestampDate2Str)
    protected val timestamp: Instant = Instant.parse(timestampStr)
    protected val timestamp2Str = "2020-01-29T14:08:30.201Z"
    protected val timestamp2: Instant = Instant.parse(timestamp2Str)
    protected val currency = "EUR"
    protected val exampleUrlBase = "https://example.com"
    protected val eventUrl = "$exampleUrlBase/event"
    protected val callbackUrl = "$exampleUrlBase/callback"
    protected val statusCallbackUrl = "$callbackUrl/status"
    protected val moCallbackUrl = "$callbackUrl/inbound-sms"
    protected val drCallbackUrl = "$callbackUrl/delivery-receipt"

    private val port = 8081
    private val wiremock: WireMockServer = WireMockServer(
        options().port(port).notifier(ConsoleNotifier(false))
    )

    val vonage = Vonage {
        apiKey(apiKey); apiSecret(apiSecret);
        applicationId(applicationId); privateKeyPath(privateKeyPath)
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

    private fun strToDate(dateStr: String): Date =
        Date(Instant.parse(dateStr.replace(' ', 'T') + 'Z').toEpochMilli())

    protected fun linksSelfHref(url: String = "$exampleUrlBase/self"): Map<String, Any> =
        mapOf("_links" to mapOf("self" to mapOf("href" to url)))

    protected enum class ContentType(val mime: String) {
        APPLICATION_JSON("application/json"),
        FORM_URLENCODED("application/x-www-form-urlencoded");

        @Override
        override fun toString(): String {
            return mime
        }
    }

    protected enum class AuthType {
        JWT, API_KEY_SECRET_HEADER, API_KEY_SECRET_QUERY_PARAMS, API_KEY_SIGNATURE_SECRET, ACCESS_TOKEN
    }

    private fun HttpMethod.toWireMockMethod(): Method = when (this) {
        HttpMethod.GET -> WireMock::get
        HttpMethod.POST -> WireMock::post
        HttpMethod.PUT -> WireMock::put
        HttpMethod.PATCH -> WireMock::patch
        HttpMethod.DELETE -> WireMock::delete
        else -> throw IllegalArgumentException("Unhandled HTTP method: $this")
    }

    private fun Map<String, Any>.toFormEncodedString(): String {
        val utf8 = StandardCharsets.UTF_8.toString()
        return entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, utf8)}=${URLEncoder.encode(value.toString(), utf8)}"
        }
    }

    private fun Map<String, Any>.toJson(): String = ObjectMapper().writeValueAsString(this)

    protected fun mockPostQueryParams(expectedUrl: String, expectedRequestParams: Map<String, Any>,
                                      authType: AuthType? = AuthType.API_KEY_SECRET_QUERY_PARAMS,
                                      status: Int = 200, expectedResponseParams: Map<String, Any>? = null) {

        val stub = post(urlPathEqualTo(expectedUrl))
        when (authType) {
            AuthType.API_KEY_SECRET_QUERY_PARAMS -> {
                stub.withFormParam(apiKeyName, equalTo(apiKey))
                    .withFormParam(apiSecretName, equalTo(apiSecret))
            }
            AuthType.JWT -> stub.withHeader(authHeaderName, matching(jwtBearerPattern))
            AuthType.ACCESS_TOKEN -> stub.withHeader(authHeaderName, equalTo(accessTokenBearer))
            AuthType.API_KEY_SECRET_HEADER -> stub.withHeader(authHeaderName, equalTo(basicSecretEncodedHeader))
            AuthType.API_KEY_SIGNATURE_SECRET -> stub.withFormParam(apiKeyName, equalTo(apiKey))
            null -> Unit
        }

        expectedRequestParams.forEach {(k, v) -> stub.withFormParam(k, equalTo(v.toString()))}

        val response = aResponse().withStatus(status)
        if (expectedResponseParams != null) {
            response.withBody(expectedResponseParams.toJson())
        }
        stub.willReturn(response)
        wiremock.stubFor(stub)
    }

    protected fun mockRequest(
        httpMethod: HttpMethod,
        expectedUrl: String,
        contentType: ContentType? = null,
        accept: ContentType? = null,
        authType: AuthType? = null,
        expectedParams: Map<String, Any>? = null): BuildingStep =
            wiremock.requestServerBuilderStep({
                urlPath equalTo expectedUrl
                headers contains "User-Agent" like "vonage-java-sdk\\/.+ java\\/.+"
                if (contentType != null) {
                    headers contains "Content-Type" equalTo contentType.mime
                }
                if (accept != null) {
                    headers contains "Accept" equalTo accept.mime
                }

                if (authType != null) {
                    when (authType) {
                        AuthType.JWT -> headers contains authHeaderName like jwtBearerPattern

                        AuthType.ACCESS_TOKEN ->
                            headers contains authHeaderName equalTo accessTokenBearer

                        AuthType.API_KEY_SECRET_HEADER ->
                            headers contains authHeaderName equalTo basicSecretEncodedHeader

                        AuthType.API_KEY_SECRET_QUERY_PARAMS -> {
                            queryParams contains apiKeyName equalTo apiKey
                            queryParams contains apiSecretName equalTo apiSecret
                        }

                        AuthType.API_KEY_SIGNATURE_SECRET -> {
                            queryParams contains apiKeyName equalTo apiKey
                            queryParams contains signatureSecretName
                        }
                    }
                }
                if (expectedParams != null) when (contentType) {
                    ContentType.APPLICATION_JSON -> {
                        body equalTo expectedParams.toJson()
                    }
                    else -> {
                        expectedParams.forEach {(k, v) -> queryParams contains k equalTo v.toString()}
                    }
                }
            }, httpMethod.toWireMockMethod())

    private fun mockP(requestMethod: HttpMethod, expectedUrl: String,
                      expectedRequestParams: Map<String, Any>? = null,
                      status: Int = 200, authType: AuthType? = AuthType.JWT,
                      contentType: ContentType?, expectedResponseParams: Map<String, Any>? = null) =

        mockRequest(requestMethod, expectedUrl, if (expectedRequestParams != null) contentType else null,
            accept = if (expectedResponseParams != null && status < 400) ContentType.APPLICATION_JSON else null,
            authType = authType, expectedRequestParams
        ).mockReturn(status, expectedResponseParams)

    protected fun mockPost(expectedUrl: String, expectedRequestParams: Map<String, Any>? = null,
                           status: Int = 200, contentType: ContentType? = ContentType.APPLICATION_JSON,
                           authType: AuthType? = AuthType.JWT, expectedResponseParams: Map<String, Any>? = null) =
        mockP(HttpMethod.POST, expectedUrl, expectedRequestParams, status, authType, contentType, expectedResponseParams)

    protected fun mockPut(expectedUrl: String, expectedRequestParams: Map<String, Any>? = null,
                           status: Int = 200, contentType: ContentType? = ContentType.APPLICATION_JSON,
                           authType: AuthType? = AuthType.JWT, expectedResponseParams: Map<String, Any>? = null) =
        mockP(HttpMethod.PUT, expectedUrl, expectedRequestParams, status, authType, contentType, expectedResponseParams)

    protected fun mockPatch(expectedUrl: String, expectedRequestParams: Map<String, Any>? = null,
                          status: Int = 200, contentType: ContentType? = ContentType.APPLICATION_JSON,
                          authType: AuthType? = AuthType.JWT, expectedResponseParams: Map<String, Any>? = null) =
        mockP(HttpMethod.PATCH, expectedUrl, expectedRequestParams, status, authType, contentType, expectedResponseParams)

    protected fun mockDelete(expectedUrl: String, authType: AuthType? = null,
                             expectedResponseParams: Map<String, Any>? = null) =
        mockRequest(HttpMethod.DELETE, expectedUrl, authType = authType)
            .mockReturn(if (expectedResponseParams == null) 204 else 200, expectedResponseParams)

    protected fun mockGet(expectedUrl: String, expectedQueryParams: Map<String, Any>? = null, status: Int = 200,
                          authType: AuthType? = AuthType.JWT, expectedResponseParams: Map<String, Any>) =
        mockRequest(HttpMethod.GET, expectedUrl, accept = ContentType.APPLICATION_JSON, authType = authType,
            expectedParams = expectedQueryParams).mockReturn(status, expectedResponseParams)


    protected fun BuildingStep.mockReturn(
            status: Int? = null, expectedBody: Map<String, Any>? = null): ReturnsStep =
        returns {
            statusCode = if
                    (status == null && expectedBody == null) 204
                    else status ?: 200

            if (expectedBody != null) {
                body = expectedBody.toJson()
                header = "Content-Type" to ContentType.APPLICATION_JSON.mime
            }
        }

    protected inline fun <reified E: VonageApiResponseException> assertApiResponseException(
            url: String, requestMethod: HttpMethod, actualCall: () -> Any) {

        assert401ApiResponseException<E>(url, requestMethod, actualCall)
        assert402ApiResponseException<E>(url, requestMethod, actualCall)
        assert429ApiResponseException<E>(url, requestMethod, actualCall)
    }

    protected inline fun <reified E: VonageApiResponseException> assertApiResponseException(
            url: String, requestMethod: HttpMethod, actualCall: () -> Any, status: Int,
            errorType: String? = null, title: String? = null,
            detail: String? = null, instance: String? = null): E {

        val responseParams = mutableMapOf<String, Any>()
        if (errorType != null) responseParams["type"] = errorType
        if (title != null) responseParams["title"] = title
        if (detail != null) responseParams["detail"] = detail
        if (instance != null) responseParams["instance"] = instance

        mockRequest(requestMethod, url).mockReturn(status, responseParams)
        val exception = assertThrows<E> { actualCall.invoke() }

        assertEquals(status, exception.statusCode)
        assertEquals(if (errorType != null) URI.create(errorType) else null, exception.type)
        assertEquals(title, exception.title)
        assertEquals(instance, exception.instance)
        assertEquals(detail, exception.detail)
        return exception
    }

    protected inline fun <reified E: VonageApiResponseException> assert401ApiResponseException(
        url: String, requestMethod: HttpMethod, actualCall: () -> Any): E =

        assertApiResponseException(url, requestMethod, actualCall, 401,
            "https://developer.nexmo.com/api-errors#unauthorized",
            "Unauthorized",
            "You did not provide correct credentials.",
            "bf0ca0bf927b3b52e3cb03217e1a1ddf"
        )

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