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

import java.net.URI
import java.net.URLEncoder
import kotlin.test.*

class NumberVerificationTest : AbstractTest() {
    private val client = vonage.numberVerification
    private val nvCheckUrl = "/camara/number-verification/v031/verify"
    private val clientAuthUrl = "https://oidc.idp.vonage.com/oauth2/auth"
    private val redirectUrl = "$exampleUrlBase/nv/redirect"
    private val code = "65536"
    private val state = "nv-$testUuidStr"

    private fun assertVerifyNumber(invocation: NumberVerification.() -> Boolean) {
        mockPostQueryParams(
            expectedUrl = "/oauth2/token",
            authType = AuthType.JWT,
            expectedRequestParams = mapOf(
                "grant_type" to "authorization_code",
                "code" to code,
                "redirect_uri" to redirectUrl
            ),
            expectedResponseParams = mapOf(
                "access_token" to accessToken,
                "token_type" to "Bearer",
                "expires" to 5400
            )
        )
        for (result in listOf(true, false, null)) {
            mockPost(
                expectedUrl = nvCheckUrl,
                authType = AuthType.ACCESS_TOKEN,
                expectedRequestParams = mapOf("phoneNumber" to "+$toNumber"),
                expectedResponseParams = if (result != null)
                    mapOf("devicePhoneNumberVerified" to result) else mapOf()
            )
            assertEquals(result ?: false, invocation(client))
        }
    }

    @Test
    fun `create verification url with and without state`() {
        val expectedUrlStr = "$clientAuthUrl?login_hint=tel%3A%2B$toNumber&scope="+
            "openid+dpv%3AFraudPreventionAndDetection%23number-verification-verify-read" +
            "&client_id=$applicationId&redirect_uri=${
                URLEncoder.encode(redirectUrl, "UTF-8")
            }&response_type=code"

        assertEquals(URI.create(expectedUrlStr), client.createVerificationUrl(toNumber, redirectUrl))

        val expectedUrlWithState = URI.create("$expectedUrlStr&state=$state")
        assertEquals(expectedUrlWithState, client.createVerificationUrl(toNumber, redirectUrl, state))
    }

    @Test
    fun `verify number all parameters`() {
        assertVerifyNumber {
            verifyNumberWithCode(toNumber, code, redirectUrl)
        }
    }

    @Test
    fun `verify number with redirect url`() {
        assertVerifyNumber {
            verifyNumberWithCode(toNumber, code, redirectUrl)
        }
    }

    @Test
    fun `verify number relying on cached redirect url`() {
        client.createVerificationUrl(altNumber, redirectUrl)

        assertVerifyNumber {
            verifyNumberWithCode(toNumber, code)
        }
    }
}