package com.vonage.client.kt

import com.vonage.client.redact.*

class Redact(private val redactClient: RedactClient) {

    fun redactSms(messageId: String, direction: RedactRequest.Type = RedactRequest.Type.OUTBOUND) {
        redactClient.redactTransaction(messageId, RedactRequest.Product.SMS, direction)
    }

    fun redactMessage(messageId: String, direction: RedactRequest.Type = RedactRequest.Type.OUTBOUND) {
        redactClient.redactTransaction(messageId, RedactRequest.Product.MESSAGES, direction)
    }

    fun redactCall(callId: String, direction: RedactRequest.Type = RedactRequest.Type.OUTBOUND) {
        redactClient.redactTransaction(callId, RedactRequest.Product.VOICE, direction)
    }

    fun redactInsight(requestId: String) {
        redactClient.redactTransaction(requestId, RedactRequest.Product.NUMBER_INSIGHTS)
    }

    fun redactVerification(requestId: String) {
        redactClient.redactTransaction(requestId, RedactRequest.Product.VERIFY)
    }
}
