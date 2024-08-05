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

import com.vonage.client.numbers.*

class Numbers(private val numbersClient: NumbersClient) {

    fun number(countryCode: String, msisdn: String) = ExistingNumber(countryCode, msisdn)

    inner class ExistingNumber internal constructor(val countryCode: String, val msisdn: String) {

        fun buy(targetApiKey: String? = null) =
            numbersClient.buyNumber(countryCode, msisdn, targetApiKey)

        fun cancel(targetApiKey: String? = null) =
            numbersClient.cancelNumber(countryCode, msisdn, targetApiKey)

        fun update(properties: UpdateNumberRequest.Builder.() -> Unit) =
            numbersClient.updateNumber(UpdateNumberRequest.builder(msisdn, countryCode).apply(properties).build())
    }

    fun listOwned(filter: ListNumbersFilter.Builder.() -> Unit = {}) =
        numbersClient.listNumbers(ListNumbersFilter.builder().apply(filter).build())

    fun searchAvailable(filter: SearchNumbersFilter.Builder.() -> Unit) =
        numbersClient.searchNumbers(SearchNumbersFilter.builder().apply(filter).build())
}
