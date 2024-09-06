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

import com.vonage.client.redact.*

/**
 * Implementation of the [Redact API](https://developer.vonage.com/en/api/redact).
 *
 * *Authentication method:* API key & secret.
 */
class Redact internal constructor(private val client: RedactClient) {

    /**
     * Redact an SMS sent using the SMS API.
     *
     * @param messageId ID of the message to redact.
     * @param direction Direction of the message to redact; either `INBOUND` or `OUTBOUND`.
     */
    fun redactSms(messageId: String, direction: RedactRequest.Type = RedactRequest.Type.OUTBOUND): Unit =
        client.redactTransaction(messageId, RedactRequest.Product.SMS, direction)

    /**
     * Redact a message sent using the Messages API.
     *
     * @param messageId UUID of the message to redact.
     * @param direction Direction of the message to redact; either `INBOUND` or `OUTBOUND`.
     */
    fun redactMessage(messageId: String, direction: RedactRequest.Type = RedactRequest.Type.OUTBOUND): Unit =
        client.redactTransaction(messageId, RedactRequest.Product.MESSAGES, direction)

    /**
     * Redact a call made using the Voice API.
     *
     * @param callId UUID of the call to redact.
     * @param direction Direction of the call to redact; either `INBOUND` or `OUTBOUND`.
     */
    fun redactCall(callId: String, direction: RedactRequest.Type = RedactRequest.Type.OUTBOUND): Unit =
        client.redactTransaction(callId, RedactRequest.Product.VOICE, direction)

    /**
     * Redact a number insight request made using the Number Insight API.
     *
     * @param requestId ID of the insight request to redact.
     */
    fun redactInsight(requestId: String): Unit =
        client.redactTransaction(requestId, RedactRequest.Product.NUMBER_INSIGHTS)

    /**
     * Redact a verification request made using the Verify API.
     *
     * @param requestId UUID of the verification request to redact.
     */
    fun redactVerification(requestId: String): Unit =
        client.redactTransaction(requestId, RedactRequest.Product.VERIFY)
}
