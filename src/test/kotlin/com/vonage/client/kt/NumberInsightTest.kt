package com.vonage.client.kt

import com.vonage.client.insight.*
import kotlin.test.*

class NumberInsightTest : AbstractTest() {
    private val niClient = vonage.numberInsight
    private val cnam = true
    private val realTimeData = true
    private val statusMessage = "Success"
    private val nationalNumber = "07712 345689"
    private val countryCode = "GB"
    private val countryCodeIso3 = "GBR"
    private val countryName = "United Kingdom"
    private val countryPrefix = "44"
    private val requestPrice = "0.035900000"
    private val refundPrice = "0.01500000"
    private val remainingBalance = "1.23456789"
    private val reachable = "reachable"


    private enum class InsightType {
        BASIC, STANDARD, ADVANCED
    }

    private fun mockInsight(type: InsightType, optionalParams: Boolean = false) {
        val expectedRequestParams = mutableMapOf<String, Any>("number" to toNumber)
        if (optionalParams) {
            expectedRequestParams["country"] = countryCode
            if (type != InsightType.BASIC) {
                expectedRequestParams["cnam"] = true
            }
            if (type == InsightType.ADVANCED) {
                expectedRequestParams["real_time_data"] = true
            }
        }

        val expectedResponseParams = mutableMapOf(
            "status" to 0,
            "status_message" to statusMessage,
            "request_id" to testUuidStr,
            "international_format_number" to toNumber,
            "national_format_number" to nationalNumber,
            "country_code" to countryCode,
            "country_code_iso3" to countryCodeIso3,
            "country_name" to countryName,
            "country_prefix" to countryPrefix
        )
        if (type != InsightType.BASIC) {
            expectedResponseParams.putAll(mapOf(
                "request_price" to requestPrice,
                "refund_price" to refundPrice,
                "remaining_balance" to remainingBalance,
                // TODO: the rest
            ))
        }
        if (type == InsightType.ADVANCED) {
            expectedResponseParams.putAll(mapOf(
                "reachable" to reachable
                // TODO: the rest
            ))
        }

        mockPostQueryParams(
            expectedUrl = "/ni/${type.name.lowercase()}/json",
            expectedRequestParams = expectedRequestParams,
            expectedResponseParams = expectedResponseParams
        )
    }

    private fun assertBasicResponse(response: BasicInsightResponse) {
        assertNotNull(response)
        assertEquals(InsightStatus.SUCCESS, response.status)
        assertEquals(statusMessage, response.statusMessage)
        assertEquals(testUuidStr, response.requestId)
        assertEquals(toNumber, response.internationalFormatNumber)
        assertEquals(nationalNumber, response.nationalFormatNumber)
        assertEquals(countryCode, response.countryCode)
        assertEquals(countryCodeIso3, response.countryCodeIso3)
        assertEquals(countryName, response.countryName)
        assertEquals(countryPrefix, response.countryPrefix)
    }

    private fun assertStandardResponse(response: StandardInsightResponse) {
        assertBasicResponse(response)
        // TODO
    }

    private fun assertAdvancedResponse(response: AdvancedInsightResponse) {
        assertStandardResponse(response)
        // TODO
    }

    @Test
    fun `basic insight required params`() {
        mockInsight(InsightType.BASIC, false)
        assertBasicResponse(niClient.basic(toNumber))
    }

    @Test
    fun `basic insight all params`() {
        mockInsight(InsightType.BASIC, true)
        assertBasicResponse(niClient.basic(toNumber, countryCode))
    }

    @Test
    fun `standard insight required params`() {
        mockInsight(InsightType.STANDARD, false)
        assertStandardResponse(niClient.standard(toNumber))
    }

    @Test
    fun `standard insight all params`() {
        mockInsight(InsightType.STANDARD, true)
        assertStandardResponse(niClient.standard(toNumber, countryCode, cnam))
    }

    @Test
    fun `advanced insight required params`() {
        mockInsight(InsightType.ADVANCED, false)
        assertStandardResponse(niClient.advanced(toNumber))
    }

    @Test
    fun `advanced insight all params`() {
        mockInsight(InsightType.ADVANCED, true)
        assertStandardResponse(niClient.advanced(toNumber, countryCode, cnam, realTimeData))
    }
}