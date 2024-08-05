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

import com.vonage.client.account.SettingsResponse
import kotlin.test.*

class AccountTest : AbstractTest() {
    private val account = vonage.account
    private val secretId = "ad6dc56f-07b5-46e1-a527-85530e625800"
    private val baseUrl = "/account"
    private val secretsBaseUrl = "/accounts/$apiKey/secrets"
    private val secretUrl = "$secretsBaseUrl/$secretId"
    private val secretObj = account.secret(secretId)

    private fun assertUpdateSettings(params: Map<String, String>, invocation: Account.() -> SettingsResponse) {
        val maxOutbound = 30
        val maxInbound = 16
        val maxCalls = 9
        mockPostQueryParams(
            expectedUrl = "$baseUrl/settings",
            authType = AuthType.API_KEY_SECRET_HEADER,
            expectedRequestParams = params,
            expectedResponseParams = mapOf(
                "mo-callback-url" to moCallbackUrl,
                "dr-callback-url" to drCallbackUrl,
                "max-outbound-request" to maxOutbound,
                "max-inbound-request" to maxInbound,
                "max-calls-per-second" to maxCalls
            )
        )

        val response = invocation.invoke(account)
        assertNotNull(response)
        assertEquals(moCallbackUrl, response.incomingSmsUrl)
        assertEquals(drCallbackUrl, response.deliveryReceiptUrl)
        assertEquals(maxOutbound, response.maxOutboundMessagesPerSecond)
        assertEquals(maxInbound, response.maxInboundMessagesPerSecond)
        assertEquals(maxCalls, response.maxApiCallsPerSecond)
    }

    @Test
    fun `get balance`() {
        val value = 10.28
        val autoReload = true

        mockGet(
            expectedUrl = "$baseUrl/get-balance",
            authType = AuthType.API_KEY_SECRET_HEADER,
            expectedResponseParams = mapOf(
                "value" to value,
                "autoReload" to autoReload
            )
        )

        val response = account.getBalance()
        assertNotNull(response)
        assertEquals(value, response.value)
        assertEquals(autoReload, response.isAutoReload)
    }

    @Test
    fun `top up balance`() {
        val trx = "8ef2447e69604f642ae59363aa5f781b"

        mockPostQueryParams(
            expectedUrl = "$baseUrl/top-up",
            authType = AuthType.API_KEY_SECRET_HEADER,
            expectedRequestParams = mapOf("trx" to trx),
            expectedResponseParams = mapOf(
                "error-code" to "200",
                "error-code-label" to "success"
            )
        )
        account.topUp(trx)
    }

    @Test
    fun `update account settings no parameters`() {
        assertUpdateSettings(mapOf()) {
            updateSettings()
        }
    }

    @Test
    fun `update account settings both parameters`() {
        assertUpdateSettings(mapOf(
            "moCallBackUrl" to moCallbackUrl,
            "drCallBackUrl" to drCallbackUrl
        )) {
            updateSettings(
                incomingSmsUrl = moCallbackUrl,
                deliverReceiptUrl = drCallbackUrl
            )
        }
    }

    @Test
    fun `list secrets`() {

    }

    @Test
    fun `create secret`() {

    }

    @Test
    fun `get secret`() {

    }

    @Test
    fun `revoke secret`() {
        mockDelete(secretUrl, AuthType.API_KEY_SECRET_HEADER)
        secretObj.delete()
    }
}