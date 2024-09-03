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

import kotlin.test.*

class ConversionTest : AbstractTest() {
    private val client = vonage.conversion
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
        client.convertSms(smsMessageId, delivered, timestampDate.toInstant())
    }

    @Test
    fun `submit sms conversion without timestamp`() {
        val delivered = false
        mockSuccess(smsMessageId, smsEndpoint, delivered, false)
        client.convertSms(smsMessageId, delivered)
    }

    @Test
    fun `submit voice conversion with timestamp`() {
        val delivered = false
        mockSuccess(callIdStr, voiceEndpoint, delivered, true)
        client.convertVoice(callIdStr, delivered, timestamp)
    }

    @Test
    fun `submit voice conversion without timestamp`() {
        val delivered = true
        mockSuccess(callIdStr, voiceEndpoint, delivered, false)
        client.convertVoice(callIdStr, delivered)
    }
}