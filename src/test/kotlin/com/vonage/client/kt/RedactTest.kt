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

import com.vonage.client.redact.RedactRequest
import com.vonage.client.redact.RedactRequest.Type.*
import kotlin.test.*

class RedactTest : AbstractTest() {
    private val transactionId = "209ab3c7536542b91e8b5aef032f6861"

    private fun testSuccess(product: String, type: RedactRequest.Type? = null, invocation: Redact.() -> Unit) {
        mockPost(
            expectedUrl = "/v1/redact/transaction", status = 204,
            authType = AuthType.API_KEY_SECRET_HEADER, expectedRequestParams = mapOf(
                "id" to transactionId,
                "product" to product
            ) + if (type != null) mapOf("type" to type.name.lowercase()) else mapOf()
        )
        invocation(vonage.redact)
    }

    @Test
    fun `redact SMS`() {
        val product = "sms"
        testSuccess(product, OUTBOUND) {
            redactSms(transactionId)
            redactSms(transactionId, OUTBOUND)
        }
        testSuccess(product, INBOUND) {
            redactSms(transactionId, INBOUND)
        }
    }

    @Test
    fun `redact message`() {
        val product = "messages"
        testSuccess(product, OUTBOUND) {
            redactMessage(transactionId)
            redactMessage(transactionId, OUTBOUND)
        }
        testSuccess(product, INBOUND) {
            redactMessage(transactionId, INBOUND)
        }
    }

    @Test
    fun `redact call`() {
        val product = "voice"
        testSuccess(product, OUTBOUND) {
            redactCall(transactionId)
            redactCall(transactionId, OUTBOUND)
        }
        testSuccess(product, INBOUND) {
            redactCall(transactionId, INBOUND)
        }
    }

    @Test
    fun `redact insight`() {
        testSuccess("number-insight") {
            redactInsight(transactionId)
        }
    }

    @Test
    fun `redact verification`() {
        testSuccess("verify") {
            redactVerification(transactionId)
        }
    }
}