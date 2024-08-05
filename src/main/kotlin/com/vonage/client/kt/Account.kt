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

class Account internal constructor(private val accountClient: AccountClient) {

    fun getBalance(): BalanceResponse = accountClient.balance

    fun topUp(transactionId: String): Unit = accountClient.topUp(transactionId)

    fun updateSettings(incomingSmsUrl: String? = null, deliverReceiptUrl: String? = null): SettingsResponse =
        accountClient.updateSettings(SettingsRequest(incomingSmsUrl, deliverReceiptUrl))

    fun listSecrets(apiKey: String? = null): List<SecretResponse> = (
            if (apiKey == null) accountClient.listSecrets()
            else accountClient.listSecrets(apiKey)
        ).secrets

    fun secret(secretId: String, apiKey: String? = null): Secret = Secret(secretId, apiKey)

    inner class Secret internal constructor(val secretId: String, val apiKey: String? = null) {

        fun get(): SecretResponse =
            if (apiKey == null) accountClient.getSecret(secretId)
            else accountClient.getSecret(apiKey, secretId)

        fun create(): SecretResponse =
            if (apiKey == null) accountClient.createSecret(secretId)
            else accountClient.createSecret(apiKey, secretId)

        fun delete(): Unit =
            if (apiKey == null) accountClient.revokeSecret(secretId)
            else accountClient.revokeSecret(apiKey, secretId)
    }
}
