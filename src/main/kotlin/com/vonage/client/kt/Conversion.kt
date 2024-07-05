package com.vonage.client.kt

import com.vonage.client.conversion.*
import java.time.Instant
import java.util.*

class Conversion(private val conversionClient: ConversionClient) {

    private fun convert(type: ConversionRequest.Type, messageId: String, delivered: Boolean,
                        timestamp: Instant? = null) =
        conversionClient.submitConversion(type, messageId, delivered,
            if (timestamp == null) null else Date.from(timestamp)
        )

    fun convertSms(messageId: String, delivered: Boolean, timestamp: Instant = Instant.now()) =
        convert(ConversionRequest.Type.SMS, messageId, delivered, timestamp)

    fun convertVoice(callId: String, delivered: Boolean, timestamp: Instant = Instant.now()) =
        convert(ConversionRequest.Type.VOICE, callId, delivered, timestamp)
}
