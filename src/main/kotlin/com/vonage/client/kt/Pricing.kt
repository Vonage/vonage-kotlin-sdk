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
 * Implementation of the [Pricing API](https://developer.vonage.com/en/api/pricing).
 *
 * *Authentication method:* API Key & secret.
 *
 * @since 1.1.0
 */
class Pricing internal constructor(private val client: AccountClient) {

    /**
     * Retrieve the cost for sending an SMS to all supported countries.
     *
     * @return The list of pricing and network information for each country.
     */
    fun listOutboundSmsPrices(): List<PricingResponse> =
        client.listPriceAllCountries(ServiceType.SMS)

    /**
     * Retrieve the cost for making a voice call to all supported countries.
     *
     * @return The list of pricing and network information for each country.
     */
    fun listOutboundVoicePrices(): List<PricingResponse> =
        client.listPriceAllCountries(ServiceType.VOICE)

    /**
     * Retrieve the cost for sending an SMS to a given country code.
     *
     * @param countryCode The two-character country code.
     *
     * @return A [PricingResponse] object with the pricing and network information.
     */
    fun getOutboundSmsPrice(countryCode: String): PricingResponse =
        client.getSmsPrice(countryCode)

    /**
     * Retrieve the cost for making a voice call to a given country code.
     *
     * @param countryCode The two-character country code.
     *
     * @return A [PricingResponse] object with the pricing and network information.
     */
    fun getOutboundVoicePriceForCountry(countryCode: String): PricingResponse =
        client.getVoicePrice(countryCode)

    /**
     * Retrieve the cost for sending an SMS to a given prefix.
     *
     * @param prefix The prefix of the phone number.
     *
     * @return The pricing and network information for each country that uses the prefix.
     */
    fun getOutboundSmsPriceForPrefix(prefix: String): List<PricingResponse> =
        client.getPrefixPrice(ServiceType.SMS, prefix).countries

    /**
     * Retrieve the cost for making a voice call to a given prefix.
     *
     * @param prefix The prefix of the phone number.
     *
     * @return The pricing and network information for each country that uses the prefix.
     */
    fun getOutboundVoicePriceForPrefix(prefix: String): List<PricingResponse> =
        client.getPrefixPrice(ServiceType.VOICE, prefix).countries
}
