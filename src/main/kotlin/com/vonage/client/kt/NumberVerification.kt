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

import com.vonage.client.camara.numberverification.*
import com.vonage.client.camara.CamaraResponseException
import com.vonage.client.auth.camara.NetworkAuthResponseException
import java.net.URI

/**
 * Implementation of the [Number Verification API](https://developer.vonage.com/en/api/camara/number-verification).
 *
 * Authentication method: JWT.
 */
class NumberVerification internal constructor(private val client: NumberVerificationClient) {
    private var redirectUri: URI? = null

    /**
     * Sets up the client for verifying the given phone number. This method will cache the provided redirect URL
     * for verification when calling #verifyNumberWithCode(String, String). The intended usage is to call this method first,
     * and follow the returned URL on the target device which the phone number is supposed to be associated with.
     * When the URL is followed, it will trigger an inbound request to the `redirectUrl` with two query parameters:
     * `CODE` and `STATE` (if provided as a parameter to this method). The code should then be extracted from the
     * query parameters and passed to the `verifyNumberWithCode` method.
     *
     * @param phoneNumber The MSISDN to verify.
     *
     * @param redirectUrl Redirect URL, as set in your Vonage application for Network APIs.
     *
     * @param state An optional string for identifying the request.
     * For simplicity, this could be set to the same value as `phoneNumber`, or it may be null.
     *
     * @return A link with appropriate parameters which should be followed on the end user's device. The link should
     * be followed when using the SIM card associated with the provided phone number. Therefore, on the target device,
     * Wi-Fi should be disabled when doing this, otherwise the result of `verifyNumber(String)` will be false.
     */
    fun createVerificationUrl(phoneNumber: String, redirectUrl: String, state: String? = null): URI {
        redirectUri = URI.create(redirectUrl)
        return client.initiateVerification(phoneNumber, redirectUri, state)
    }

    /**
     * Verifies the given phone number with the provided code. This method should be called after the user has followed
     * the URL returned by `createVerificationUrl(String, String, String)`. The code should be extracted from the
     * query parameters of the URL and passed to this method. If `createVerificationUrl` has not been called first,
     * then the `redirectUrl` parameter should be provided here. If it has, then it can be omitted.
     *
     * @param phoneNumber The MSISDN to verify.
     *
     * @param code The code extracted from the query parameters of the URL returned by `createVerificationUrl(String, String, String)`.
     *
     * @param redirectUrl Redirect URL, as set in your Vonage application for Network APIs.
     * This defaults to the value passed in the `createVerificationUrl` method, but can be overridden here.
     *
     * @return `true` if the device that followed the link was using the SIM card associated with the phone number
     * provided in `createVerificationUrl`, `false` otherwise (e.g. it was unknown, the link was not followed,
     * the device that followed the link didn't use the SIM card with that phone number when doing so).
     *
     * @throws NetworkAuthResponseException If there was an error exchanging the code for an access token when
     * using the Vonage Network Auth API.
     *
     * @throws CamaraResponseException If there was an error in communicating with the Number Verification API.
     */
    fun verifyNumberWithCode(phoneNumber: String, code: String, redirectUrl: String? = null): Boolean {
        if (redirectUrl != null) redirectUri = URI.create(redirectUrl)
        return client.verifyNumber(phoneNumber, redirectUri, code)
    }
}
