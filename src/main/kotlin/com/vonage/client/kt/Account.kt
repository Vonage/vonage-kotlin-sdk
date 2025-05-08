/*
 *   Copyright 2025 Vonage
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
 *
 * *Authentication method:* API key and secret.
 */
class Account internal constructor(private val client: AccountClient) {

    /**
     * Obtains the current account remaining balance.
     *
     * @return A [BalanceResponse] object containing the current account balance.
     * @throws [AccountResponseException] If the balance cannot be retrieved.
     */
    fun getBalance(): BalanceResponse = client.balance

    /**
     * You can top up your account using this API when you have enabled auto-reload in the dashboard.
     * The amount added by the top-up operation will be the same amount as was added in the payment when
     * auto-reload was enabled. Your account balance is checked every 5-10 minutes and if it falls below the
     * threshold and auto-reload is enabled, then it will be topped up automatically. Use this method if you
     * need to top up at times when your credit may be exhausted more quickly than the auto-reload may occur.
     *
     * @param transactionId The top-up transaction ID.
     *
     * @throws [AccountResponseException] If the top-up operation fails.
     */
    fun topUp(transactionId: String): Unit = client.topUp(transactionId)

    /**
     * Updates the top-level account settings. Namely, the URLs for incoming SMS and delivery receipts.
     *
     * @param incomingSmsUrl The URL to which incoming SMS messages are sent when using SMS API.
     * @param deliverReceiptUrl The URL to which delivery receipts are sent when using SMS API.
     *
     * @return The updated account settings.
     *
     * @throws [AccountResponseException] If the account settings could not be updated.
     */
    fun updateSettings(incomingSmsUrl: String? = null, deliverReceiptUrl: String? = null): SettingsResponse =
        client.updateSettings(SettingsRequest(incomingSmsUrl, deliverReceiptUrl))

    /**
     * Call this method to work with account secrets.
     *
     * @param apiKey (OPTIONAL) The account API key to manage secrets for. If not provided,
     * the default API key (as supplied in the top-level [Vonage] client) will be used.
     *
     * @return A [Secrets] object with methods to interact with account secrets.
     */
    fun secrets(apiKey: String? = null): Secrets = Secrets(apiKey)

    /**
     * Class for working with account secrets.
     *
     * @property apiKey The account API key to manage secrets for. If not provided,
     * the default API key (as supplied in the top-level [Vonage] client) will be used.
     */
    inner class Secrets internal constructor(val apiKey: String? = null) {

        /**
         * Retrieves secrets for the account.
         *
         * @return A list of secrets details.
         *
         * @throws [AccountResponseException] If the secrets cannot be retrieved.
         */
        fun list(): List<SecretResponse> = (
                if (apiKey == null) client.listSecrets()
                else client.listSecrets(apiKey)
        )

        /**
         * Creates a new secret for the account.
         *
         * @param secret The secret value to associate with the API key, which must follow these rules:
         * - Minimum 8 characters
         * - Maximum 25 characters
         * - Minimum 1 lower case character
         * - Minimum 1 upper case character
         * - Minimum 1 digit
         *
         * @return The created secret's metadata.
         *
         * @throws [AccountResponseException] If the secret cannot be created.
         */
        fun create(secret: String): SecretResponse =
            if (apiKey == null) client.createSecret(secret)
            else client.createSecret(apiKey, secret)

        /**
         * Retrieves a secret by its ID.
         *
         * @param secretId ID of the secret to retrieve.
         *
         * @return The secret's metadata.
         *
         * @throws [AccountResponseException] If the secret cannot be retrieved.
         */
        fun get(secretId: String): SecretResponse =
            if (apiKey == null) client.getSecret(secretId)
            else client.getSecret(apiKey, secretId)

        /**
         * Deletes a secret by its ID.
         *
         * @param secretId ID of the secret to delete.
         *
         * @throws [AccountResponseException] If the secret cannot be deleted.
         */
        fun delete(secretId: String): Unit =
            if (apiKey == null) client.revokeSecret(secretId)
            else client.revokeSecret(apiKey, secretId)
    }
}
