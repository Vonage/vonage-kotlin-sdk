package com.vonage.client.kt

import kotlin.test.*

class ConversionTest : AbstractTest() {
    private val conversionClient = vonage.conversion
    private val smsEndpoint = "sms"
    private val voiceEndpoint = "voice"

    private fun mockSuccess(id: String, endpoint: String, delivered: Boolean, includeTimestamp: Boolean) {
        mockPostQueryParams("/conversions/$endpoint", mapOf(
            "message-id" to id,
            "delivered" to delivered
        ) + if (includeTimestamp) mapOf("timestamp" to timestampDateStr) else mapOf())
    }

    @Test
    fun `submit sms conversion with timestamp`() {
        val delivered = true
        mockSuccess(smsMessageId, smsEndpoint, delivered, true)
        conversionClient.convertSms(smsMessageId, delivered, timestamp)
    }

    @Test
    fun `submit sms conversion without timestamp`() {
        val delivered = false
        mockSuccess(smsMessageId, smsEndpoint, delivered, false)
        conversionClient.convertSms(smsMessageId, delivered)
    }

    @Test
    fun `submit voice conversion with timestamp`() {
        val delivered = false
        mockSuccess(callIdStr, voiceEndpoint, delivered, true)
        conversionClient.convertVoice(callIdStr, delivered, timestamp)
    }

    @Test
    fun `submit voice conversion without timestamp`() {
        val delivered = true
        mockSuccess(callIdStr, voiceEndpoint, delivered, false)
        conversionClient.convertVoice(callIdStr, delivered)
    }
}