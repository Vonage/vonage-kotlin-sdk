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

import com.vonage.client.auth.camara.FraudPreventionDetectionScope
import kotlin.test.*

class SimSwapTest : AbstractTest() {
    private val simSwapClient = vonage.simSwap
    private val baseSimSwapUrl = "/camara/sim-swap/v040"
    private val checkSimSwapUrl = "$baseSimSwapUrl/check"
    private val retrieveSimSwapDateUrl = "$baseSimSwapUrl/retrieve-date"
    private val simSwapNumber = toNumber
    private val phoneNumberMap = mapOf("phoneNumber" to simSwapNumber)
    private val authReqId = "arid/0dadaeb4-7c79-4d39-b4b0-5a6cc08bf537"

    private fun mockBackendAuth(scope: FraudPreventionDetectionScope) {
        mockPostQueryParams(
            expectedUrl = "/oauth2/bc-authorize",
            authType = AuthType.JWT,
            expectedRequestParams = mapOf(
                "login_hint" to "tel:+$simSwapNumber",
                "scope" to "openid dpv:FraudPreventionAndDetection#$scope"
            ),
            expectedResponseParams = mapOf(
                "auth_req_id" to authReqId,
                "expires_in" to 120,
                "interval" to 3
            )
        )
        mockPostQueryParams(
            expectedUrl = "/oauth2/token",
            authType = AuthType.JWT,
            expectedRequestParams = mapOf(
                "grant_type" to "urn:openid:params:grant-type:ciba",
                "auth_req_id" to authReqId
            ),
            expectedResponseParams = mapOf(
                "access_token" to accessToken,
                "refresh_token" to "xyz789012ghi",
                "token_type" to "Bearer",
                "expires" to 3600
            ),
        )
    }

    private fun assertCheckSimSwap(maxAge: Int = 240, invocation: SimSwap.() -> Boolean) {
        mockBackendAuth(FraudPreventionDetectionScope.CHECK_SIM_SWAP)
        for (result in listOf(true, false, null)) {
            mockPost(
                expectedUrl = checkSimSwapUrl,
                authType = AuthType.ACCESS_TOKEN,
                expectedRequestParams = phoneNumberMap + mapOf("maxAge" to maxAge),
                expectedResponseParams = if (result != null) mapOf("swapped" to result) else mapOf()
            )
            assertEquals(result ?: false, invocation.invoke(simSwapClient))
        }
    }

    private fun assertRetrieveSimSwapDate(includeResponse: Boolean) {
        mockBackendAuth(FraudPreventionDetectionScope.RETRIEVE_SIM_SWAP_DATE)
        mockPost(
            expectedUrl = retrieveSimSwapDateUrl,
            authType = AuthType.ACCESS_TOKEN,
            expectedRequestParams = phoneNumberMap,
            expectedResponseParams = if (includeResponse) mapOf("latestSimChange" to timestampStr) else mapOf()
        )
        assertEquals(if (includeResponse) timestamp else null, simSwapClient.retrieveSimSwapDate(simSwapNumber))
    }

    @Test
    fun `check sim swap number only`() {
        assertCheckSimSwap {
            checkSimSwap(simSwapNumber)
        }
    }

    @Test
    fun `check sim swap with maxAge`() {
        val maxAge = 1200
        assertCheckSimSwap(maxAge) {
            checkSimSwap(simSwapNumber, maxAge)
        }
    }

    @Test
    fun `retrieve sim swap date success`() {
        assertRetrieveSimSwapDate(true)
    }

    @Test
    fun `retrieve sim swap date unknown`() {
        assertRetrieveSimSwapDate(false)
    }
}