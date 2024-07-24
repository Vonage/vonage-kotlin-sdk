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

    /*fun advancedAsync(number: String, countryCode: String? = null, cnam: Boolean = false,
                      callbackUrl: String? = null) =
        niClient.getAdvancedNumberInsight(AdvancedInsightRequest.builder().async(true)
            .number(number).country(countryCode).cnam(cnam).callback(callbackUrl).build()
        )*/
}
