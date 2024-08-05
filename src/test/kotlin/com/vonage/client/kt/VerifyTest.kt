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

import com.vonage.client.common.HttpMethod
import com.vonage.client.verify2.Channel
import com.vonage.client.verify2.VerifyResponseException
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.UUID
import kotlin.test.*

class VerifyTest : AbstractTest() {
    private val verifyClient = vonage.verify
    private val baseUrl = "/v2/verify"
    private val requestIdStr = "c11236f4-00bf-4b89-84ba-88b25df97315"
    private val requestId = UUID.fromString(requestIdStr)
    private val requestIdUrl = "$baseUrl/$requestIdStr"
    private val timeout = 60
    private val fraudCheck = false
    private val sandbox = true
    private val codeLength = 5
    private val code = "1228864"
    private val locale = "ja-jp"
    private val whatsappNumber = "447700400080"
    private val appHash = "ABC123def45"
    private val toEmail = "alice@example.com"
    private val fromEmail = "bob@example.org"
    private val checkUrl = "https://api.nexmo.com/v2/verify/$requestIdStr/silent-auth/redirect"
    private val redirectUrl = "https://acme-app.com/sa/redirect"
    private val checkCodeRequestParams = mapOf("code" to code)

    private fun assertVerifyResponseException(url: String, requestMethod: HttpMethod, actualCall: () -> Any) {
        assertApiResponseException<VerifyResponseException>(url, requestMethod, actualCall)
        if (url.contains(requestIdStr)) {
            assertApiResponseException<VerifyResponseException>(url, requestMethod, actualCall,
                404, errorType = "https://developer.vonage.com/api-errors#not-found",
                title = "Not Found", instance = "bf0ca0bf927b3b52e3cb03217e1a1ddf",
                detail = "Request $requestIdStr was not found or it has been verified already."
            )
            if (requestMethod != HttpMethod.DELETE) {
                assertApiResponseException<VerifyResponseException>(url, requestMethod, actualCall,
                    409, errorType = "https://www.developer.vonage.com/api-errors/verify#conflict",
                    title = "Conflict", instance = "738f9313-418a-4259-9b0d-6670f06fa82d",
                    detail = "Concurrent verifications to the same number are not allowed."
                )
            }
        }
    }

    @Test
    fun `send verification single workflow required parameters`() {
        for (channel in Channel.entries) {
            mockPost(
                baseUrl, status = 202, expectedRequestParams = mapOf(
                    "brand" to brand, "workflow" to listOf(
                        mapOf(
                            "channel" to channel.toString(),
                            "to" to if (channel == Channel.EMAIL) toEmail else toNumber
                        ) + when (channel) {
                            Channel.WHATSAPP, Channel.WHATSAPP_INTERACTIVE -> mapOf("from" to whatsappNumber)
                            else -> mapOf()
                        }
                    )
                ),
                expectedResponseParams = mapOf("request_id" to requestIdStr) +
                        if (channel == Channel.SILENT_AUTH) mapOf("check_url" to checkUrl) else mapOf()
            )

            val response = verifyClient.sendVerification(brand) {
                when (channel) {
                    Channel.VOICE -> voice(toNumber)
                    Channel.SMS -> sms(toNumber)
                    Channel.SILENT_AUTH -> silentAuth(toNumber)
                    Channel.EMAIL -> email(toEmail)
                    Channel.WHATSAPP -> whatsapp(toNumber, whatsappNumber)
                    Channel.WHATSAPP_INTERACTIVE -> whatsappCodeless(toNumber, whatsappNumber)
                }
            }
            assertNotNull(response)
            assertEquals(requestId, response.requestId)
            if (channel == Channel.SILENT_AUTH) {
                assertEquals(URI.create(checkUrl), response.checkUrl)
            }
            else {
                assertNull(response.checkUrl)
            }
        }
    }

    @Test
    fun `send verification all workflows and parameters`() {
        mockPost(baseUrl,
            expectedRequestParams = mapOf(
                "brand" to brand,
                "client_ref" to clientRef,
                "channel_timeout" to timeout,
                "code_length" to codeLength,
                "locale" to "ja-jp",
                "fraud_check" to fraudCheck,
                "workflow" to listOf(
                    mapOf(
                        "channel" to "silent_auth",
                        "to" to toNumber,
                        "sandbox" to sandbox,
                        "redirect_url" to redirectUrl
                    ),
                    mapOf(
                        "channel" to "voice",
                        "to" to altNumber
                    ),
                    mapOf(
                        "channel" to "sms",
                        "to" to toNumber,
                        "from" to altNumber,
                        "content_id" to contentId,
                        "entity_id" to entityId,
                        "app_hash" to appHash
                    ),
                    mapOf(
                        "channel" to "email",
                        "to" to toEmail,
                        "from" to fromEmail
                    ),
                    mapOf(
                        "channel" to "whatsapp",
                        "to" to altNumber,
                        "from" to whatsappNumber
                    ),
                    mapOf(
                        "channel" to "whatsapp_interactive",
                        "to" to toNumber,
                        "from" to whatsappNumber
                    )
                )
            ),
            expectedResponseParams = mapOf(
                "request_id" to requestIdStr,
                "check_url" to checkUrl
            ),
            status = 202
        )

        val response = verifyClient.sendVerification {
            brand(brand); clientRef(clientRef); channelTimeout(timeout)
            fraudCheck(fraudCheck); codeLength(codeLength); locale(locale)
            silentAuth(toNumber, sandbox, redirectUrl); voice(altNumber); sms(toNumber) {
            entityId(entityId); contentId(contentId); appHash(appHash); from(altNumber)
        }; email(toEmail, fromEmail)
            whatsapp(altNumber, whatsappNumber); whatsappCodeless(toNumber, whatsappNumber)
        }

        assertNotNull(response)
        assertEquals(requestId, response.requestId)
        assertEquals(URI.create(checkUrl), response.checkUrl)
    }

    @Test
    fun `cancel verification`() {
        mockDelete(requestIdUrl)
        verifyClient.cancelVerification(requestIdStr)
        verifyClient.cancelVerification(requestId)
        assertVerifyResponseException(requestIdUrl, HttpMethod.DELETE) {
            verifyClient.cancelVerification(requestIdStr)
        }
    }

    @Test
    fun `next workflow`() {
        val expectedUrl = "$requestIdUrl/next-workflow"
        mockPost(expectedUrl)
        verifyClient.nextWorkflow(requestIdStr)
        verifyClient.nextWorkflow(requestId)
        assertVerifyResponseException(expectedUrl, HttpMethod.POST) {
            verifyClient.nextWorkflow(requestId)
        }
        assertVerifyResponseException(expectedUrl, HttpMethod.POST) {
            verifyClient.nextWorkflow(requestIdStr)
        }
    }

    @Test
    fun `check valid verification code`() {
        val call: () -> Boolean = {verifyClient.isValidVerificationCode(requestIdStr, code)}

        mockPost(requestIdUrl, checkCodeRequestParams, 200)
        assertTrue(call.invoke())
        verifyClient.checkVerificationCode(requestIdStr, code)
        verifyClient.checkVerificationCode(requestId, code)
        assertTrue(verifyClient.isValidVerificationCode(requestId, code))


        val title = "Invalid Code"

        mockPost(requestIdUrl, checkCodeRequestParams, 400, expectedResponseParams = mapOf(
            "title" to title,
            "type" to "https://www.developer.vonage.com/api-errors/verify#invalid-code",
            "detail" to "The code you provided does not match the expected value."
        ))
        assertFalse(call.invoke())

        mockPost(requestIdUrl, checkCodeRequestParams, 410, expectedResponseParams = mapOf(
            "title" to title,
            "type" to "https://www.developer.vonage.com/api-errors/verify#expired",
            "detail" to "An incorrect code has been provided too many times. Workflow terminated."
        ))
        assertFalse(call.invoke())

        assertVerifyResponseException(requestIdUrl, HttpMethod.POST, call)
        assertVerifyResponseException(requestIdUrl, HttpMethod.POST) {
            verifyClient.checkVerificationCode(requestIdStr, code)
        }
    }
}