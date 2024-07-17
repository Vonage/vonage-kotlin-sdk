package com.vonage.client.kt

import com.vonage.client.verify.*
import java.util.*
import kotlin.test.*

class VerifyLegacyTest : AbstractTest() {
    private val verifyClient = vonage.verifyLegacy
    private val requestId = "abcdef0123456789abcdef0123456789"
    private val payee = "Acme Inc"
    private val amount = 48.31

    private fun assertVerifySucccess(params: Map<String, Any>, invocation: VerifyLegacy.() -> VerifyResponse) {
        mockPostQueryParams("/verify/json", params, expectedResponseParams = mapOf(
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
        assertVerifySucccess(mapOf("brand" to payee, "number" to toNumber)) {
            verify(toNumber, payee)
        }
    }

    @Test
    fun `verify request success all parameters`() {
        assertVerifySucccess(mapOf(
            "brand" to payee, "number" to toNumber,
            "sender_id" to altNumber,
            "pin_expiry" to 60,
            "next_event_wait" to 750,
            "country" to "GB",
            "lg" to "en-gb",
            "workflow_id" to 2
        )) {
            verify(toNumber, payee) {
                senderId(altNumber)
                pinExpiry(60); nextEventWait(750);
                locale(Locale.UK); country("GB");
                workflow(VerifyRequest.Workflow.SMS_SMS_TTS)
            }
        }
    }

    @Test
    fun `psd2 request`() {

    }

    @Test
    fun `check verification code`() {

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