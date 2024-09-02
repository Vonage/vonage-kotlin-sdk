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

import com.vonage.client.verify.*
import java.math.BigDecimal
import java.util.*
import kotlin.test.*

class VerifyLegacyTest : AbstractTest() {
    private val verifyClient = vonage.verifyLegacy
    private val requestId = "abcdef0123456789abcdef0123456789"
    private val existingRequest = verifyClient.request(requestId)
    private val eventId = "0A00000012345678"
    private val accountId = "abcdef01"
    private val payee = "Acme Inc"
    private val amount = 48.31
    private val pinExpiry = 60
    private val nextEventWait = 750
    private val pinCode = "987123"
    private val codeLength = 6
    private val price = "0.10000000"
    private val estimatedPriceMessagesSent = "0.03330000"

    private fun getBaseUri(endpoint: String): String =
        "/verify/$endpoint/json".replace("//", "/")

    private fun assertVerify(params: Map<String, Any>, invocation: VerifyLegacy.() -> VerifyResponse) {
        val expectedUrl = getBaseUri(if (params.containsKey("payee")) "psd2" else "")
        val successResponse =
        mockPostQueryParams(expectedUrl, params, expectedResponseParams = mapOf(
            "request_id" to requestId,
            "status" to "0"
        ))
        val successParsed = invocation(verifyClient)
        assertNotNull(successParsed)
        assertEquals(requestId, successParsed.requestId)
        assertEquals(VerifyStatus.OK, successParsed.status)
        assertEquals(existingRequest, verifyClient.request(successParsed))

        val errorText = "Your request is incomplete and missing the mandatory parameter `number`"
        mockPostQueryParams(expectedUrl, params, expectedResponseParams = mapOf(
            "request_id" to requestId,
            "status" to "2",
            "error_text" to errorText,
            "network" to networkCode
        ))
        val failureParsed = invocation(verifyClient)
        assertNotNull(failureParsed)
        assertEquals(requestId, failureParsed.requestId)
        assertEquals(VerifyStatus.MISSING_PARAMS, failureParsed.status)
        assertEquals(errorText, failureParsed.errorText)
        assertEquals(networkCode, failureParsed.network)
    }

    private fun invokeControl(command: VerifyControlCommand) = when(command) {
            VerifyControlCommand.CANCEL -> existingRequest.cancel()
            VerifyControlCommand.TRIGGER_NEXT_EVENT -> existingRequest.advance()
        }

    private fun assertControl(command: VerifyControlCommand) {
        val cmdStr = command.name.lowercase()
        val expectedUrl = getBaseUri("control")
        val expectedRequestParams = mapOf(
            "request_id" to requestId,
            "cmd" to cmdStr
        )
        mockPostQueryParams(expectedUrl, expectedRequestParams,
            expectedResponseParams = mapOf(
                "status" to "0",
                "command" to cmdStr
            )
        )
        val parsedSuccess = invokeControl(command)
        assertNotNull(parsedSuccess)
        assertEquals(command, parsedSuccess.command)
        assertEquals(VerifyStatus.OK, VerifyStatus.fromInt(parsedSuccess.status.toInt()))
        assertNull(parsedSuccess.errorText)

        val errorText = "Your account does not have sufficient credit to process this request."
        mockPostQueryParams(expectedUrl, expectedRequestParams,
            expectedResponseParams = mapOf(
                "status" to "9",
                "error_text" to errorText
            )
        )

        try {
            val parsedFailure = invokeControl(command)
            fail("Expected VerifyException but got $parsedFailure")
        }
        catch (ex: VerifyException) {
            assertEquals(VerifyStatus.PARTNER_QUOTA_EXCEEDED, VerifyStatus.fromInt(ex.status.toInt()))
            assertEquals(errorText, ex.errorText)
        }
    }

    private fun assertSearch(vararg requestIds: String) {
        val single = requestIds.size == 1
        mockPostQueryParams(
            expectedUrl = getBaseUri("search"),
            expectedRequestParams = if (single)
                mapOf("request_id" to requestIds.first()) else
                mapOf("request_ids" to requestIds.last()),  // Will contain duplicates otherwise
            expectedResponseParams = mapOf(
                "status" to "0",
                "verification_requests" to listOf(
                    mapOf("status" to "EXPIRED"),
                    mapOf(
                        "request_id" to requestId,
                        "account_id" to accountId,
                        "number" to toNumber,
                        "currency" to currency,
                        "sender_id" to payee,
                        "date_submitted" to timestampDateStr,
                        "date_finalized" to timestampDate2Str,
                        "first_event_date" to timestampDateStr,
                        "last_event_date" to timestampDate2Str,
                        "status" to "IN PROGRESS",
                        "price" to price,
                        "estimated_price_messages_sent" to estimatedPriceMessagesSent
                    ),
                    mapOf()
                )
            )
        )
        val response = if (single) existingRequest.search() else verifyClient.search(*requestIds)
        assertNotNull(response)
        assertNull(response.errorText)
        assertEquals(3, response.verificationRequests.size)
        assertNotNull(response.verificationRequests[2])
        assertEquals(VerifyDetails.Status.EXPIRED, response.verificationRequests[0].status)
        val detail = response.verificationRequests[1]
        assertNotNull(detail)
        assertEquals(requestId, detail.requestId)
        assertEquals(accountId, detail.accountId)
        assertEquals(toNumber, detail.number)
        assertEquals(currency, detail.currency)
        assertEquals(payee, detail.senderId)
        assertEquals(timestampDate, detail.dateSubmitted)
        assertEquals(timestampDate2, detail.dateFinalized)
        assertEquals(timestampDate, detail.firstEventDate)
        assertEquals(timestampDate2, detail.lastEventDate)
        assertEquals(VerifyDetails.Status.IN_PROGRESS, detail.status)
        assertEquals(BigDecimal(price), detail.price)
        assertEquals(BigDecimal(estimatedPriceMessagesSent), detail.estimatedPriceMessagesSent)
    }

    @Test
    fun `existing request hashCode is based on the requestId`() {
        assertEquals(requestId.hashCode(), existingRequest.hashCode())
        assertEquals(existingRequest, verifyClient.request(requestId))
        assertEquals(existingRequest, existingRequest)
        assertFalse(existingRequest.equals(requestId))
    }

    @Test
    fun `verify request success required parameters`() {
        assertVerify(mapOf("brand" to payee, "number" to toNumber)) {
            verify(toNumber, payee)
        }
    }

    @Test
    fun `verify request all parameters`() {
        assertVerify(mapOf(
            "brand" to payee, "number" to toNumber,
            "sender_id" to altNumber,
            "pin_expiry" to pinExpiry,
            "pin_code" to pinCode,
            "next_event_wait" to nextEventWait,
            "country" to "GB",
            "lg" to "en-gb",
            "workflow_id" to 2
        )) {
            verify(toNumber, payee) {
                senderId(altNumber); pinCode(pinCode)
                pinExpiry(pinExpiry); nextEventWait(nextEventWait)
                locale(Locale.UK); country("GB")
                workflow(VerifyRequest.Workflow.SMS_SMS_TTS)
            }
        }
    }

    @Test
    fun `psd2 request required parameters`() {
        assertVerify(mapOf(
            "number" to toNumber, "amount" to amount, "payee" to payee
        )) {
            psd2Verify(toNumber, amount, payee)
        }
    }

    @Test
    fun `psd2 request all parameters`() {
        val country = "DE"
        assertVerify(mapOf(
            "number" to toNumber,
            "amount" to amount,
            "payee" to payee,
            "code_length" to codeLength,
            "pin_expiry" to pinExpiry,
            "next_event_wait" to nextEventWait,
            "country" to country,
            "lg" to "de-${country.lowercase()}",
            "workflow_id" to 5

        )) {
            psd2Verify(toNumber, amount, payee) {
                length(codeLength); pinExpiry(pinExpiry)
                nextEventWait(nextEventWait); country(country)
                locale(Locale.GERMANY)
                workflow(Psd2Request.Workflow.SMS_TTS)
            }
        }
    }

    @Test
    fun `check verification code`() {
        val expectedUrl = getBaseUri("check")
        val expectedRequestParams = mapOf(
            "request_id" to requestId,
            "code" to pinCode
        )
        mockPostQueryParams(expectedUrl, expectedRequestParams,
            expectedResponseParams = mapOf(
                "request_id" to requestId,
                "event_id" to eventId,
                "status" to 0,
                "price" to price,
                "currency" to currency,
                "estimated_price_messages_sent" to estimatedPriceMessagesSent
            )
        )

        val parsedSuccess = existingRequest.check(pinCode)
        assertNotNull(parsedSuccess)
        assertNull(parsedSuccess.errorText)
        assertEquals(requestId, parsedSuccess.requestId)
        assertEquals(eventId, parsedSuccess.eventId)
        assertEquals(VerifyStatus.OK, parsedSuccess.status)
        assertEquals(currency, parsedSuccess.currency)
        assertEquals(BigDecimal(estimatedPriceMessagesSent), parsedSuccess.estimatedPriceMessagesSent)

        val errorText = "The code inserted does not match the expected value"
        mockPostQueryParams(expectedUrl, expectedRequestParams,
            expectedResponseParams = mapOf(
                "request_id" to requestId,
                "status" to 16,
                "error_text" to errorText
            )
        )

        val parsedFailure = existingRequest.check(pinCode)
        assertNotNull(parsedFailure)
        assertEquals(requestId, parsedFailure.requestId)
        assertEquals(VerifyStatus.INVALID_CODE, parsedFailure.status)
        assertEquals(errorText, parsedFailure.errorText)
    }

    @Test
    fun `cancel verification`() {
        assertControl(VerifyControlCommand.CANCEL)
    }

    @Test
    fun `advance verification`() {
        assertControl(VerifyControlCommand.TRIGGER_NEXT_EVENT)
    }

    @Test
    fun `search single request`() {
        assertSearch(requestId)
    }

    @Test
    fun `search multiple requests`() {
        assertSearch(requestId, textHexEncoded, testUuidStr)
    }
}