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

import com.vonage.client.insight.*

/**
 * Implementation of the [Number Insight API](https://developer.vonage.com/en/api/number-insight).
 *
 * *Authentication method:* API key & secret.
 */
class NumberInsight internal constructor(private val client: InsightClient) {

    /**
     * Obtain basic insight about a number.
     *
     * @param number The phone number to look up in E.164 format.
     *
     * @param countryCode (OPTIONAL) The two-character country code in ISO 3166-1 alpha-2 format.
     *
     * @return Basic details about the number and insight metadata.
     */
    fun basic(number: String, countryCode: String? = null): BasicInsightResponse =
        client.getBasicNumberInsight(number, countryCode)

    /**
     * Obtain standard insight about a number.
     *
     * @param number The phone number to look up in E.164 format.
     *
     * @param countryCode (OPTIONAL) The two-character country code in ISO 3166-1 alpha-2 format.
     *
     * @param cnam (OPTIONAL) Whether the name of the person who owns the phone number should be looked up
     * and returned in the response. Set to true to receive phone number owner name in the response. This
     * feature is available for US numbers only and incurs an additional charge.
     *
     * @return Standard details about the number and insight metadata.
     */
    fun standard(number: String, countryCode: String? = null, cnam: Boolean? = null): StandardInsightResponse =
        client.getStandardNumberInsight(StandardInsightRequest.builder()
            .number(number).country(countryCode).cnam(cnam).build()
        )

    /**
     * Obtain advanced insight about a number synchronously. This is not recommended due to potential timeouts.
     *
     * @param number The phone number to look up in E.164 format.
     *
     * @param countryCode (OPTIONAL) The two-character country code in ISO 3166-1 alpha-2 format.
     *
     * @param cnam (OPTIONAL) Whether the name of the person who owns the phone number should be looked up
     * and returned in the response. Set to true to receive phone number owner name in the response. This
     * feature is available for US numbers only and incurs an additional charge.
     *
     * @param realTimeData (OPTIONAL) Whether to receive real-time data back in the response.
     *
     * @return Advanced details about the number and insight metadata.
     */
    fun advanced(number: String, countryCode: String? = null, cnam: Boolean = false,
                 realTimeData: Boolean = false): AdvancedInsightResponse =
        client.getAdvancedNumberInsight(AdvancedInsightRequest.builder().async(false)
            .number(number).country(countryCode).cnam(cnam).realTimeData(realTimeData).build()
        )

    /**
     * Obtain advanced insight about a number asynchronously. This is recommended to avoid timeouts.
     *
     * @param number The phone number to look up in E.164 format.
     *
     * @param callbackUrl The URL to which the response will be sent.
     *
     * @param countryCode (OPTIONAL) The two-character country code in ISO 3166-1 alpha-2 format.
     *
     * @param cnam (OPTIONAL) Whether the name of the person who owns the phone number should be looked up
     * and returned in the response. Set to true to receive phone number owner name in the response. This
     * feature is available for US numbers only and incurs an additional charge.
     */
    fun advancedAsync(number: String, callbackUrl: String, countryCode: String? = null, cnam: Boolean = false) {
        client.getAdvancedNumberInsight(
            AdvancedInsightRequest.builder().async(true)
                .number(number).country(countryCode).cnam(cnam).callback(callbackUrl).build()
        )
    }
}
