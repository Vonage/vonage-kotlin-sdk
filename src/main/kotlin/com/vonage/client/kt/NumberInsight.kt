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

import com.vonage.client.insight.*

class NumberInsight(private val niClient: InsightClient) {

    fun basic(number: String, countryCode: String? = null): BasicInsightResponse =
        niClient.getBasicNumberInsight(number, countryCode)

    fun standard(number: String, countryCode: String? = null, cnam: Boolean? = null): StandardInsightResponse =
        niClient.getStandardNumberInsight(StandardInsightRequest.builder()
            .number(number).country(countryCode).cnam(cnam).build()
        )

    fun advanced(number: String, countryCode: String? = null, cnam: Boolean = false,
                 realTimeData: Boolean = false): AdvancedInsightResponse =
        niClient.getAdvancedNumberInsight(AdvancedInsightRequest.builder().async(false)
            .number(number).country(countryCode).cnam(cnam).realTimeData(realTimeData).build()
        )

    fun advancedAsync(number: String, callbackUrl: String, countryCode: String? = null, cnam: Boolean = false) {
        niClient.getAdvancedNumberInsight(
            AdvancedInsightRequest.builder().async(true)
                .number(number).country(countryCode).cnam(cnam).callback(callbackUrl).build()
        )
    }
}
