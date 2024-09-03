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

import com.vonage.client.conversion.*
import java.time.Instant
import java.util.*

/**
 * Implementation of the [Conversion API](https://developer.vonage.com/en/api/Conversion).
 */
class Conversion internal constructor(private val client: ConversionClient) {

    private fun convert(type: ConversionRequest.Type,
                        messageId: String, delivered: Boolean, timestamp: Instant?): Unit =
        client.submitConversion(type, messageId, delivered,
            if (timestamp != null) Date.from(timestamp) else null
        )

    /**
     * Submit conversion for an SMS message.
     *
     * @param messageId The message ID.
     * @param delivered `true` if the message was delivered, `false` otherwise.
     * @param timestamp (OPTIONAL) Timestamp of the conversion.
     */
    fun convertSms(messageId: String, delivered: Boolean, timestamp: Instant? = null): Unit =
        convert(ConversionRequest.Type.SMS, messageId, delivered, timestamp)

    /**
     * Submit conversion for a voice call.
     *
     * @param callId The call UUID.
     * @param delivered `true` if the call was received, `false` otherwise.
     * @param timestamp (OPTIONAL) Timestamp of the conversion.
     */
    fun convertVoice(callId: String, delivered: Boolean, timestamp: Instant? = null): Unit =
        convert(ConversionRequest.Type.VOICE, callId, delivered, timestamp)
}
