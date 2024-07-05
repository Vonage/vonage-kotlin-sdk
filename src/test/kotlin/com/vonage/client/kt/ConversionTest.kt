package com.vonage.client.kt

import kotlin.test.*

class ConversionTest : AbstractTest() {
    private val conversionClient = vonage.conversion

    private fun mockSuccess(id: String, endpoint: String, delivered: Boolean) {
        mockPostQueryParams("/conversions/$endpoint", mapOf(
            "message-id" to id,
            "delivered" to delivered,
            "timestamp" to timestampDateStr
        ))
    }

    @Test
    fun `submit sms conversion`() {
        val delivered = true
        mockSuccess(smsMessageId, "sms", delivered)
        conversionClient.convertSms(smsMessageId, delivered, timestamp)
    }

    @Test
    fun `submit voice conversion`() {
        val delivered = false
        mockSuccess(callIdStr, "voice", delivered)
        conversionClient.convertVoice(callIdStr, delivered, timestamp)
    }
}