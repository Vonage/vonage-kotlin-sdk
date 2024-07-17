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

    private fun assertVerifySuccess(params: Map<String, Any>, invocation: VerifyLegacy.() -> VerifyResponse) {
        val expectedUrl = getBaseUri(if (params.containsKey("payee")) "psd2" else "")
        mockPostQueryParams(expectedUrl, params, expectedResponseParams = mapOf(
                "request_id" to requestId,
                "status" to "0"
        ))
        val parsed = invocation.invoke(verifyClient)
        assertNotNull(parsed)
        assertEquals(requestId, parsed.requestId)
        assertEquals(VerifyStatus.OK, parsed.status)
    }

    @Test
    fun `verify request success required parameters`() {
        assertVerifySuccess(mapOf("brand" to payee, "number" to toNumber)) {
            verify(toNumber, payee)
        }
    }

    @Test
    fun `verify request success all parameters`() {
        assertVerifySuccess(mapOf(
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
    fun `psd2 request success required parameters`() {
        assertVerifySuccess(mapOf(
            "number" to toNumber, "amount" to amount, "payee" to payee
        )) {
            psd2Verify(toNumber, amount, payee)
        }
    }

    @Test
    fun `psd2 request success all parameters`() {
        val country = "AT"
        assertVerifySuccess(mapOf(
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
                locale(Locale.of("de", country))
                workflow(Psd2Request.Workflow.SMS_TTS)
            }
        }
    }

    @Test
    fun `check verification code success`() {
        mockPostQueryParams(getBaseUri("check"), expectedRequestParams = mapOf(
            "request_id" to requestId,
            "code" to pinCode
        ), expectedResponseParams = mapOf(
            "request_id" to requestId,
            "event_id" to eventId,
            "status" to 0,
            "price" to price,
            "currency" to currency,
            "estimated_price_messages_sent" to estimatedPriceMessagesSent
        ))
        val response = existingRequest.check(pinCode)
        assertNotNull(response)
        assertEquals(requestId, response.requestId)
        assertEquals(eventId, response.eventId)
        assertEquals(VerifyStatus.OK, response.status)
        assertEquals(currency, response.currency)
        assertEquals(BigDecimal(estimatedPriceMessagesSent), response.estimatedPriceMessagesSent)
    }

    @Test
    fun `cancel verification`() {

    }

    @Test
    fun `advance verification`() {

    }

    @Test
    fun `search single request`() {

    }

    @Test
    fun `search multiple requests`() {

    }
}