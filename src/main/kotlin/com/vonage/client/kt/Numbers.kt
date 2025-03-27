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

import com.vonage.client.numbers.*

/**
 * Implementation of the [Numbers API](https://developer.vonage.com/en/api/numbers).
 *
 * *Authentication method:* API key & secret.
 */
class Numbers internal constructor(private val client: NumbersClient) {

    /**
     * Call this method to work with an existing number.
     *
     * @param countryCode The two-character country code of the number.
     * @param msisdn The phone number in E.164 format.
     *
     * @return An [ExistingNumber] object with methods to interact with the number.
     */
    fun number(countryCode: String, msisdn: String) = ExistingNumber(countryCode, msisdn)

    /**
     * Class for working with an existing number.
     *
     * @property countryCode The two-character country code of the number.
     * @property msisdn The phone number in E.164 format.
     */
    inner class ExistingNumber internal constructor(val countryCode: String, val msisdn: String) {

        /**
         * Purchase the number.
         *
         * @param targetApiKey (OPTIONAL) The API key of the subaccount to assign the number to.
         * If unspecified (the default), the action will be performed on the main account.
         *
         * @throws [NumbersResponseException] If the number could not be purchased.
         */
        fun buy(targetApiKey: String? = null): Unit =
            client.buyNumber(countryCode, msisdn, targetApiKey)

        /**
         * Cancel the number.
         *
         * @param targetApiKey (OPTIONAL) The API key of the subaccount to cancel the number on.
         * If unspecified (the default), the action will be performed on the main account.
         *
         * @throws [NumbersResponseException] If the number could not be cancelled.
         */
        fun cancel(targetApiKey: String? = null): Unit =
            client.cancelNumber(countryCode, msisdn, targetApiKey)

        /**
         * Update the number's assignment on your account.
         *
         * @param properties A lambda function for specifying the properties to update.
         *
         * @throws [NumbersResponseException] If the number could not be updated.
         */
        fun update(properties: UpdateNumberRequest.Builder.() -> Unit): Unit =
            client.updateNumber(UpdateNumberRequest.builder(msisdn, countryCode).apply(properties).build())
    }

    /**
     * List numbers owned by the account.
     *
     * @param filter (OPTIONAL) A lambda function for specifying the filter properties.
     *
     * @return The list of owned numbers matching the filter criteria.
     *
     * @throws [NumbersResponseException] If the list could not be retrieved.
     */
    fun listOwned(filter: ListNumbersFilter.Builder.() -> Unit = {}): List<OwnedNumber> =
        client.listNumbers(ListNumbersFilter.builder().apply(filter).build())

    /**
     * Search for numbers that are available to purchase.
     *
     * @param filter A lambda function for specifying the search filter properties.
     *
     * @return The list of available numbers matching the filter critera.
     *
     * @throws [NumbersResponseException] If the search request could not be completed.
     */
    fun searchAvailable(filter: SearchNumbersFilter.Builder.() -> Unit): List<AvailableNumber> =
        client.searchNumbers(SearchNumbersFilter.builder().apply(filter).build()).numbers.asList()
}
