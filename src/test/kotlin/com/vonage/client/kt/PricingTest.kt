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

import com.vonage.client.account.Network
import com.vonage.client.account.PricingResponse
import kotlin.test.*

class PricingTest : AbstractTest() {
    private val client = vonage.pricing
    private val authType = AuthType.API_KEY_SECRET_HEADER
    private val countryCode = "CA"
    private val mobileNetwork = mapOf(
        "type" to "mobile",
        "price" to "0.00590000",
        "currency" to currency,
        "mcc" to "302",
        "mnc" to "530",
        "networkCode" to "302530",
        "networkName" to "Keewaytinook Okimakanak"
    )
    private val canada = mapOf(
        "countryName" to "Canada",
        "countryDisplayName" to "Canada",
        "countryCode" to countryCode,
        "currency" to "EUR",
        "defaultPrice" to "0.00620000",
        "dialingPrefix" to "1",
        "networks" to listOf(mobileNetwork)
    )
    private val allCountries = mapOf(
        "count" to 243,
        "countries" to listOf(canada, emptyMap())
    )

    private fun assertEqualsSampleMobileNetwork(parsed: Network) {
        assertEquals(Network.Type.MOBILE, parsed.type)
        assertEquals(0.00590000, parsed.price.toDouble())
        assertEquals(currency, parsed.currency)
        assertEquals("302", parsed.mcc)
        assertEquals("530", parsed.mnc)
        assertEquals("302530", parsed.code)
        assertEquals("Keewaytinook Okimakanak", parsed.name)
    }

    private fun assertEqualsEmptyNetwork(parsed: Network) {
        assertNotNull(parsed)
        assertNull(parsed.name)
        assertNull(parsed.code)
        assertNull(parsed.currency)
        assertNull(parsed.mcc)
        assertNull(parsed.mnc)
        assertNull(parsed.price)
        assertNull(parsed.type)
    }

    private fun assertEqualsSampleCountry(parsed: PricingResponse) {
        assertEquals("EUR", parsed.currency)
        val country = parsed.country
        assertEquals("Canada", country.name)
        assertEquals("Canada", country.displayName)
        assertEquals("CA", country.code)
        assertEquals(0.00620000, parsed.defaultPrice.toDouble())
        assertEquals("1", parsed.dialingPrefix)
        assertEquals(1, parsed.networks.size)
        assertEqualsSampleMobileNetwork(parsed.networks[0])
    }

    private fun assertEqualsEmptyCountry(parsed: PricingResponse) {
        assertNotNull(parsed)
        assertNull(parsed.currency)
        val country = parsed.country
        assertNotNull(country)
        assertNull(country.name)
        assertNull(country.displayName)
        assertNull(country.code)
        assertNull(parsed.defaultPrice)
        assertNull(parsed.dialingPrefix)
        assertNull(parsed.networks)
    }

    @Test
    fun `list all outbound prices`() {
        val baseUrl = "/account/get-full-pricing/outbound"
        mockGet("$baseUrl/sms", authType = authType, expectedResponseParams = allCountries)

        val smsResponse = client.listOutboundSmsPrices()
        assertNotNull(smsResponse)
        assertEquals(2, smsResponse.size)
        assertEqualsSampleCountry(smsResponse[0])
        assertEqualsEmptyCountry(smsResponse[1])

        mockGet("$baseUrl/voice", authType = authType, expectedResponseParams = allCountries)
        assertEquals(smsResponse, client.listOutboundVoicePrices())
    }

    @Test
    fun `get outbound price for country`() {
        val baseUrl = "/account/get-pricing/outbound"
        mockGet("$baseUrl/sms", authType = authType, expectedResponseParams = canada)

        val smsResponse = client.getOutboundSmsPrice(countryCode)
        assertNotNull(smsResponse)
        assertEqualsSampleCountry(smsResponse)

        mockGet("$baseUrl/voice", authType = authType, expectedResponseParams = canada)
        assertEquals(smsResponse, client.getOutboundVoicePriceForCountry(countryCode))
    }

    @Test
    fun `get outbound price for prefix`() {
        val prefix = "44"
        val baseUrl = "/account/get-prefix-pricing/outbound"
        mockGet("$baseUrl/sms", authType = authType,
            expectedQueryParams = mapOf("prefix" to prefix),
            expectedResponseParams = allCountries
        )

        val smsResponse = client.getOutboundSmsPriceForPrefix(prefix)
        assertNotNull(smsResponse)
        assertEquals(2, smsResponse.size)
        assertEqualsSampleCountry(smsResponse[0])
        assertEqualsEmptyCountry(smsResponse[1])

        mockGet("$baseUrl/voice", authType = authType,
            expectedQueryParams = mapOf("prefix" to prefix),
            expectedResponseParams = allCountries
        )
        assertEquals(smsResponse, client.getOutboundVoicePriceForPrefix(prefix))
    }
}