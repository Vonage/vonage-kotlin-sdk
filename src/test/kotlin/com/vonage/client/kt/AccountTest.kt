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

import com.vonage.client.account.*
import com.vonage.client.common.HttpMethod
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class AccountTest : AbstractTest() {
    private val account = vonage.account
    private val authType = AuthType.API_KEY_SECRET_HEADER
    private val secretId = "ad6dc56f-07b5-46e1-a527-85530e625800"
    private val trx = "8ef2447e69604f642ae59363aa5f781b"
    private val baseUrl = "/account"
    private val secretsUrl = "${baseUrl}s/$apiKey/secrets"
    private val secretsAltUrl = "${baseUrl}s/$apiKey2/secrets"
    private val secretUrl = "$secretsUrl/$secretId"
    private val altSecretUrl = "$secretsAltUrl/$secretId"
    private val secretsNoApiKey = account.secrets()
    private val secretsWithApiKey = account.secrets(apiKey2)
    private val errorCode = 401
    private val secretResponse = linksSelfHref(secretUrl) + mapOf(
        "id" to secretId,
        "created_at" to timestampStr
    )
    private val secretRequest = mapOf("secret" to secret)
    private val errorResponse = mapOf(
        "error-code" to errorCode.toString(),
        "error-code-label" to "authentication failed"
    )

    private fun assertUpdateSettings(params: Map<String, String>, invocation: Account.() -> SettingsResponse) {
        val maxOutbound = 30
        val maxInbound = 16
        val maxCalls = 9
        mockPostQueryParams(
            expectedUrl = "$baseUrl/settings",
            authType = authType,
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

    private fun assertSecretResponse(response: SecretResponse) {
        assertNotNull(response)
        assertEquals(secretId, response.id)
        assertEquals(timestamp, response.created)
    }

    private fun getSecretsObj(withApiKey: Boolean) =
        if (withApiKey) secretsWithApiKey else secretsNoApiKey

    private fun getSecretUrl(withApiKey: Boolean) =
        if (withApiKey) altSecretUrl else secretUrl

    private fun getSecretsUrl(withApiKey: Boolean) =
        if (withApiKey) secretsAltUrl else secretsUrl

    private fun assertListSecrets(withApiKey: Boolean) {
        val url = getSecretsUrl(withApiKey)
        mockGet(
            expectedUrl = url,
            authType = authType,
            expectedResponseParams = linksSelfHref() + mapOf(
                "_embedded" to mapOf("secrets" to listOf(
                    secretResponse,
                    mapOf()
                ))
            )
        )
        val invocation = { getSecretsObj(withApiKey).list() }

        val response = invocation.invoke()
        assertNotNull(response)
        assertEquals(2, response.size)
        assertSecretResponse(response[0])
        val blank = response[1]
        assertNotNull(blank)
        assertNull(blank.created)
        assertNull(blank.id)

        assert401ApiResponseException<AccountResponseException>(url, HttpMethod.GET, invocation)
    }

    private fun assertCreateSecret(withApiKey: Boolean) {
        val url = getSecretsUrl(withApiKey)
        val invocation = { getSecretsObj(withApiKey).create(secret) }
        mockPost(
            expectedUrl = url, authType = authType,
            expectedRequestParams = secretRequest,
            status = 201, expectedResponseParams = secretResponse
        )
        assertSecretResponse(invocation.invoke())
        assert401ApiResponseException<AccountResponseException>(url, HttpMethod.POST, invocation)
    }

    private fun assertGetSecret(withApiKey: Boolean) {
        val url = getSecretUrl(withApiKey)
        mockGet(
            expectedUrl = url, authType = authType,
            expectedResponseParams = secretResponse
        )
        val invocation = { getSecretsObj(withApiKey).get(secretId) }
        assertSecretResponse(invocation.invoke())
        assert401ApiResponseException<AccountResponseException>(url, HttpMethod.GET, invocation)
    }

    private fun assertDeleteSecret(withApiKey: Boolean) {
        val url = getSecretUrl(withApiKey)
        val invocation = { getSecretsObj(withApiKey).delete(secretId) }
        mockDelete(url, authType)
        invocation.invoke()

        mockRequest(HttpMethod.DELETE, expectedUrl = url, authType = authType)
            .mockReturn(status = errorCode, errorResponse)

        assertThrows<AccountResponseException>(invocation)
    }

    @Test
    fun `get balance success`() {
        val value = 10.28
        val autoReload = true

        mockGet(
            expectedUrl = "$baseUrl/get-balance",
            authType = authType,
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
    fun `get balance error`() {
        mockGet(
            expectedUrl = "$baseUrl/get-balance",
            status = errorCode, authType = authType,
            expectedResponseParams = errorResponse
        )
        assertThrows<AccountResponseException> { account.getBalance() }
    }

    @Test
    fun `top up balance success`() {
        mockPostQueryParams(
            expectedUrl = "$baseUrl/top-up",
            authType = authType,
            expectedRequestParams = mapOf("trx" to trx),
            expectedResponseParams = mapOf(
                "error-code" to "200",
                "error-code-label" to "success"
            )
        )
        account.topUp(trx)
    }

    @Test
    fun `top up balance error`() {
        mockPostQueryParams(
            expectedUrl = "$baseUrl/top-up",
            authType = authType, status = errorCode,
            expectedRequestParams = mapOf("trx" to trx),
            expectedResponseParams = errorResponse
        )

        assertThrows<AccountResponseException> { account.topUp(trx) }
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
    fun `list secrets default api key`() {
        assertListSecrets(false)
    }

    @Test
    fun `list secrets alternate api key`() {
        assertListSecrets(true)
    }

    @Test
    fun `create secret default api key`() {
        assertCreateSecret(false)
    }

    @Test
    fun `create secret alternate api key`() {
        assertCreateSecret(true)
    }

    @Test
    fun `get secret default api key`() {
        assertGetSecret(false)
    }

    @Test
    fun `get secret alternate api key`() {
        assertGetSecret(true)
    }

    @Test
    fun `revoke secret default api key`() {
        assertDeleteSecret(false)
        assertNull(secretsNoApiKey.apiKey)
    }

    @Test
    fun `revoke secret alternate api key`() {
        assertDeleteSecret(true)
        assertEquals(apiKey2, secretsWithApiKey.apiKey)
    }
}