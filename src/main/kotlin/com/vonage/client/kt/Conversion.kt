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

class Conversion internal constructor(private val conversionClient: ConversionClient) {

    private fun convert(type: ConversionRequest.Type,
                        messageId: String, delivered: Boolean, timestamp: Instant?): Unit =
        conversionClient.submitConversion(type, messageId, delivered,
            if (timestamp != null) Date.from(timestamp) else null
        )

    fun convertSms(messageId: String, delivered: Boolean, timestamp: Instant? = null): Unit =
        convert(ConversionRequest.Type.SMS, messageId, delivered, timestamp)

    fun convertVoice(callId: String, delivered: Boolean, timestamp: Instant? = null): Unit =
        convert(ConversionRequest.Type.VOICE, callId, delivered, timestamp)
}
