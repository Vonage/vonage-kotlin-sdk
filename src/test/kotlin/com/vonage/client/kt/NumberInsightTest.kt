package com.vonage.client.kt

import com.vonage.client.insight.*
import com.vonage.client.insight.CarrierDetails.NetworkType
import java.math.BigDecimal
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
    private val reachable = Reachability.REACHABLE
    private val ported = PortedStatus.ASSUMED_PORTED
    private val callerType = CallerType.CONSUMER
    private val firstName = "Max"
    private val lastName = "Mustermann"
    private val callerName = "$firstName $lastName"
    private val originalNetworkCode = "12345"
    private val originalName = "Acme Inc"
    private val originalCountry = "CA"
    private val originalNetworkType = NetworkType.PAGER
    private val currentNetworkCode = networkCode
    private val currentName = "Nexmo"
    private val currentCountry = countryCode
    private val currentNetworkType = NetworkType.LANDLINE_PREMIUM
    private val lookupOutcomeMessage = "Partial success - some fields populated"
    private val validNumber = Validity.INFERRED_NOT_VALID
    private val active = true
    private val handsetStatus = "On"


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

        val expectedResponseParams = mutableMapOf<String, Any>(
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
            val callerIdentity = mapOf(
                "caller_name" to callerName,
                "last_name" to lastName,
                "first_name" to firstName,
                "caller_type" to callerType.name.lowercase()
            )

            expectedResponseParams.putAll(mapOf(
                "request_price" to requestPrice,
                "refund_price" to refundPrice,
                "remaining_balance" to remainingBalance,
                "current_carrier" to mapOf(
                    "network_code" to currentNetworkCode,
                    "name" to currentName,
                    "country" to currentCountry,
                    "network_type" to currentNetworkType
                ),
                "ported" to ported.name.lowercase(),
                "original_carrier" to mapOf(
                    "network_code" to originalNetworkCode,
                    "name" to originalName,
                    "country" to originalCountry,
                    "network_type" to originalNetworkType
                ),
                "caller_identity" to callerIdentity
            ))

            if (type == InsightType.STANDARD) {
                expectedResponseParams.putAll(callerIdentity)
            }
        }
        if (type == InsightType.ADVANCED) {
            expectedResponseParams.putAll(mapOf(
                "reachable" to reachable,
                "lookup_outcome" to 1,
                "lookup_outcome_message" to lookupOutcomeMessage,
                "valid_number" to validNumber.name.lowercase(),
                "real_time_data" to mapOf(
                    "active_status" to active,
                    "handset_status" to handsetStatus
                )
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
        assertEquals(BigDecimal(requestPrice), response.requestPrice)
        assertEquals(BigDecimal(refundPrice), response.refundPrice)
        assertEquals(BigDecimal(remainingBalance), response.remainingBalance)
        assertEquals(ported, response.ported)
        if (response::class == StandardInsightResponse::class.java) {
            assertEquals(firstName, response.firstName)
            assertEquals(lastName, response.lastName)
            assertEquals(callerName, response.callerName)
            assertEquals(callerType, response.callerType)
        }
        val callerIdentity = response.callerIdentity
        assertNotNull(callerIdentity)
        assertEquals(firstName, callerIdentity.firstName)
        assertEquals(lastName, callerIdentity.lastName)
        assertEquals(callerName, callerIdentity.name)
        assertEquals(callerType, callerIdentity.type)
        val currentCarrier = response.currentCarrier
        assertNotNull(currentCarrier)
        assertEquals(currentName, currentCarrier.name)
        assertEquals(currentCountry, currentCarrier.country)
        assertEquals(currentNetworkType, currentCarrier.networkType)
        assertEquals(currentNetworkCode, currentCarrier.networkCode)
        val originalCarrier = response.originalCarrier
        assertNotNull(originalCarrier)
        assertEquals(originalName, originalCarrier.name)
        assertEquals(originalCountry, originalCarrier.country)
        assertEquals(originalNetworkType, originalCarrier.networkType)
        assertEquals(originalNetworkCode, originalCarrier.networkCode)
    }

    private fun assertAdvancedResponse(response: AdvancedInsightResponse) {
        assertStandardResponse(response)
        assertEquals(reachable, response.reachability)
        assertEquals(LookupOutcome.PARTIAL_SUCCESS, response.lookupOutcome)
        assertEquals(lookupOutcomeMessage, response.lookupOutcomeMessage)
        assertEquals(validNumber, response.validNumber)
        val rtd = response.realTimeData
        assertNotNull(rtd)
        assertEquals(active, rtd.activeStatus)
        assertEquals(handsetStatus, rtd.handsetStatus)
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
        assertAdvancedResponse(niClient.advanced(toNumber))
    }

    @Test
    fun `advanced insight all params`() {
        mockInsight(InsightType.ADVANCED, true)
        assertAdvancedResponse(niClient.advanced(toNumber, countryCode, cnam, realTimeData))
    }
}