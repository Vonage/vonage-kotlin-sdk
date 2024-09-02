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

/**
 * Implementation of the [Account API](https://developer.vonage.com/en/api/account).
 */
class Account internal constructor(private val client: AccountClient) {

    /**
     * Obtains the current account remaining balance.
     *
     * @return A [BalanceResponse] object containing the current account balance.
     * @throws [AccountResponseException] If the balance cannot be retrieved.
     */
    fun getBalance(): BalanceResponse = client.balance

    fun topUp(transactionId: String): Unit = client.topUp(transactionId)

    fun updateSettings(incomingSmsUrl: String? = null, deliverReceiptUrl: String? = null): SettingsResponse =
        client.updateSettings(SettingsRequest(incomingSmsUrl, deliverReceiptUrl))

    fun secrets(apiKey: String? = null): Secrets = Secrets(apiKey)

    inner class Secrets internal constructor(val apiKey: String? = null) {

        fun list(): List<SecretResponse> = (
                if (apiKey == null) client.listSecrets()
                else client.listSecrets(apiKey)
            ).secrets

        fun create(secret: String): SecretResponse =
            if (apiKey == null) client.createSecret(secret)
            else client.createSecret(apiKey, secret)

        fun get(secretId: String): SecretResponse =
            if (apiKey == null) client.getSecret(secretId)
            else client.getSecret(apiKey, secretId)

        fun delete(secretId: String): Unit =
            if (apiKey == null) client.revokeSecret(secretId)
            else client.revokeSecret(apiKey, secretId)
    }
}
