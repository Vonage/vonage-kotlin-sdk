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
import com.vonage.client.numbers.UpdateNumberRequest.CallbackType
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.*

class NumbersTest : AbstractTest() {
    private val client = vonage.numbers
    private val authType = AuthType.API_KEY_SECRET_HEADER
    private val targetApiKey = "1a2345b7"
    private val moSmppSysType = "inbound"
    private val buyEndpoint = "buy"
    private val cancelEndpoint = "cancel"
    private val updateEndpoint = "update"
    private val featureNames = Feature.entries.map(Feature::name)
    private val pattern = "1337*"
    private val count = 1247
    private val size = 25
    private val index = 6
    private val errorCode = 401
    private val existingNumber = client.number(country, toNumber)
    private val baseRequestParams = mapOf(
        "country" to existingNumber.countryCode,
        "msisdn" to existingNumber.msisdn
    )
    private val targetApiKeyMap = mapOf("target_api_key" to targetApiKey)
    private val successResponseMap = mapOf(
        "error-code" to "200",
        "error-code-label" to "success"
    )
    private val errorResponse = mapOf(
        "error-code" to errorCode.toString(),
        "error-code-label" to "authentication failed"
    )

    private fun assertThrowsGet(url: String, invocation: Numbers.() -> Any) {
        mockGet(expectedUrl = url,
            status = errorCode, authType = authType,
            expectedResponseParams = errorResponse
        )
        assertThrows<NumbersResponseException> { invocation.invoke(client) }
    }

    private fun assertThrowsPost(endpoint: String, invocation: Numbers.ExistingNumber.() -> Any) {
        mockPost(expectedUrl = "/number/$endpoint",
            status = errorCode, authType = authType,
            expectedResponseParams = errorResponse
        )
        assertThrows<NumbersResponseException> { invocation.invoke(existingNumber) }
    }

    private fun mockAction(endpoint: String, additionalParams: Map<String, String> = mapOf()) {
        mockPostQueryParams(expectedUrl = "/number/$endpoint",
            expectedRequestParams = baseRequestParams + additionalParams,
            authType = authType, expectedResponseParams = successResponseMap
        )
    }

    private fun assertOwnedNumbers(params: Map<String, Any>, invocation: Numbers.() -> ListNumbersResponse) {
        val type = Type.MOBILE_LVN
        val voiceCallbackType = CallbackType.SIP
        val messagesCallbackValue = "aaaaaaaa-bbbb-cccc-dddd-0123456789ab"
        val url = "/account/numbers"
        
        mockGet(
            expectedUrl = url,
            expectedQueryParams = params,
            authType = authType,
            expectedResponseParams = mapOf(
                "count" to count,
                "numbers" to listOf(
                    mapOf(),
                    baseRequestParams + mapOf(
                        "moHttpUrl" to moCallbackUrl,
                        "type" to type.name.lowercase().replace('_', '-'),
                        "features" to featureNames,
                        "messagesCallbackType" to "app",
                        "messagesCallbackValue" to messagesCallbackValue,
                        "voiceCallbackType" to voiceCallbackType.name.lowercase(),
                        "voiceCallbackValue" to sipUri,
                        "app_id" to applicationId
                    )
                )
            )
        )

        val response = invocation.invoke(client)
        assertNotNull(response)
        assertEquals(count, response.count)
        val numbers = response.numbers
        assertNotNull(numbers)
        assertEquals(2, numbers.size)

        val empty = numbers[0]
        assertNotNull(empty)
        assertNull(empty.msisdn)
        assertNull(empty.country)
        assertNull(empty.voiceCallbackType)
        assertNull(empty.voiceCallbackValue)
        assertNull(empty.messagesCallbackValue)
        assertNull(empty.moHttpUrl)
        assertNull(empty.type)
        assertNull(empty.features)

        val main = numbers[1]
        assertNotNull(main)
        assertEquals(country, main.country)
        assertEquals(toNumber, main.msisdn)
        assertEquals(moCallbackUrl, main.moHttpUrl)
        assertEquals(type, Type.fromString(main.type))
        assertEquals(featureNames, main.features.toList())
        assertEquals(UUID.fromString(messagesCallbackValue), main.messagesCallbackValue)
        assertEquals(voiceCallbackType, CallbackType.fromString(main.voiceCallbackType))
        assertEquals(sipUri, main.voiceCallbackValue)

        assertThrowsGet(url, invocation)
    }

    private fun assertAvailableNumbers(params: Map<String, Any>, invocation: Numbers.() -> SearchNumbersResponse) {
        val landline = "44800123456"
        val url = "/number/search"
        mockGet(
            expectedUrl = url,
            expectedQueryParams = params,
            authType = authType,
            expectedResponseParams = mapOf(
                "count" to count,
                "numbers" to listOf(
                    mapOf("cost" to "1.29"),
                    mapOf(
                        "country" to country,
                        "msisdn" to landline,
                        "type" to "landline-toll-free",
                        "cost" to "3.80",
                        "features" to listOf("VOICE")
                    ),
                    baseRequestParams + mapOf(
                        "features" to listOf("SMS", "MMS"),
                        "type" to "mobile-lvn"
                    )
                )
            )
        )

        val response = invocation.invoke(client)
        assertNotNull(response)
        assertEquals(count, response.count)
        val numbers = response.numbers
        assertNotNull(numbers)
        assertEquals(3, numbers.size)

        val costOnly = numbers[0]
        assertNotNull(costOnly)
        assertEquals(1.29, costOnly.cost.toDouble())
        assertNull(costOnly.type)
        assertNull(costOnly.country)
        assertNull(costOnly.msisdn)
        assertNull(costOnly.features)

        val main = numbers[1]
        assertNotNull(main)
        assertEquals(Type.LANDLINE_TOLL_FREE, Type.fromString(main.type))
        assertEquals(3.80, main.cost.toDouble())
        assertEquals(landline, main.msisdn)
        assertEquals(country, main.country)
        val mainFeatures = main.features
        assertNotNull(mainFeatures)
        assertEquals(1, mainFeatures.size)
        assertEquals(Feature.VOICE, Feature.fromString(mainFeatures[0]))

        val mobile = numbers[2]
        assertEquals(country, mobile.country)
        assertEquals(toNumber, mobile.msisdn)
        assertEquals(Type.MOBILE_LVN, Type.fromString(mobile.type))
        val mobileFeatures = mobile.features
        assertNotNull(mobileFeatures)
        assertEquals(2, mobileFeatures.size)
        assertEquals(Feature.SMS, Feature.fromString(mobileFeatures[0]))
        assertEquals(Feature.MMS, Feature.fromString(mobileFeatures[1]))
        assertNull(mobile.cost)

        assertThrowsGet(url, invocation)
    }

    @Test
    fun `buy number`() {
        mockAction(buyEndpoint)
        existingNumber.buy()
        assertThrowsPost(buyEndpoint) { buy() }
    }

    @Test
    fun `buy number with target api key`() {
        mockAction(buyEndpoint, targetApiKeyMap)
        existingNumber.buy(targetApiKey)
        assertThrowsPost(buyEndpoint) { buy(targetApiKey) }
    }

    @Test
    fun `cancel number`() {
        mockAction(cancelEndpoint)
        existingNumber.cancel()
        assertThrowsPost(cancelEndpoint) { cancel() }
    }

    @Test
    fun `cancel number with target api key`() {
        mockAction(cancelEndpoint, targetApiKeyMap)
        existingNumber.cancel(targetApiKey)
        assertThrowsPost(cancelEndpoint) { cancel(targetApiKey) }
    }

    @Test
    fun `update no parameters`() {
        mockAction(updateEndpoint)
        existingNumber.update {}
        assertThrowsPost(updateEndpoint) { update {} }
    }

    @Test
    fun `update all parameters`() {
        mockAction(updateEndpoint, mapOf(
            "app_id" to applicationId,
            "moHttpUrl" to moCallbackUrl,
            "moSmppSysType" to moSmppSysType,
            "voiceStatusCallback" to statusCallbackUrl,
            "voiceCallbackType" to "tel",
            "voiceCallbackValue" to altNumber
        ))
        existingNumber.update {
            applicationId(applicationId)
            moHttpUrl(moCallbackUrl); moSmppSysType(moSmppSysType)
            voiceStatusCallback(statusCallbackUrl)
            voiceCallback(CallbackType.TEL, altNumber)
        }
    }

    @Test
    fun `list owned numbers no parameters`() {
        assertOwnedNumbers(mapOf()) { listOwned() }
    }

    @Test
    fun `list owned numbers all parameters`() {
        val hasApplication = true
        assertOwnedNumbers(mapOf(
            "country" to country,
            "application_id" to applicationId,
            "has_application" to hasApplication,
            "pattern" to pattern,
            "search_pattern" to 2,
            "size" to size,
            "index" to index
        )) {
            listOwned {
                country(country)
                applicationId(applicationId)
                hasApplication(hasApplication)
                pattern(SearchPattern.ENDS_WITH, pattern)
                size(size); index(index)
            }
        }
    }

    @Test
    fun `search available numbers no parameters`() {
        assertAvailableNumbers(mapOf()) {
            searchAvailable {  }
        }
    }

    @Test
    fun `search available numbers all parameters`() {
        assertAvailableNumbers(mapOf(
            "country" to country,
            "pattern" to pattern,
            "search_pattern" to 0,
            "features" to featureNames.joinToString(","),
            "size" to size,
            "index" to index
        )) {
            searchAvailable {
                country(country); size(size); index(index)
                pattern(SearchPattern.STARTS_WITH, pattern)
                features(Feature.SMS, Feature.MMS, Feature.VOICE)
            }
        }
    }
}