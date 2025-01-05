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

import com.vonage.client.auth.camara.NetworkAuthResponseException
import com.vonage.client.camara.simswap.*
import com.vonage.client.camara.CamaraResponseException
import java.time.Instant

/**
 * Implementation of the [Sim Swap API](https://developer.vonage.com/en/api/camara/sim-swap).
 *
 * *Authentication method:* JWT.
 */
class SimSwap internal constructor(private val client: SimSwapClient) {

    /**
     * Check if a SIM swap has been performed during the specified past period for the given phone number.
     *
     * @param phoneNumber Subscriber number in E.164 format (starting with country code). Optionally prefixed with '+'.
     *
     * @param maxAgeHours Period in hours to be checked for SIM swap. Must be between 1 and 2400.
     * Default is 10 days (240 hours).
     *
     * @return `true` if the SIM card has been swapped during the period within the provided age.
     *
     * @throws NetworkAuthResponseException If an error was encountered in the OAuth 2 flow when
     * using the Vonage Network Auth API to obtain the access token.
     *
     * @throws CamaraResponseException If the request was unsuccessful. This could be for the following reasons:
     * - **400**: Invalid request arguments.
     * - **401**: Request not authenticated due to missing, invalid, or expired credentials.
     * - **403**: Client does not have sufficient permissions to perform this action.
     * - **404**: SIM Swap can't be checked because the phone number is unknown.
     * - **409**: Another request is created for the same MSISDN.
     * - **502**: Bad gateway.
     */
    fun checkSimSwap(phoneNumber: String, maxAgeHours: Int = 240): Boolean =
        client.checkSimSwap(phoneNumber, maxAgeHours)

    /**
     * Get timestamp of last MSISDN to IMSI pairing change for a mobile user account.
     *
     * @param phoneNumber Subscriber number in E.164 format (starting with country code). Optionally prefixed with '+'.
     *
     * @return Time of the latest SIM swap performed as an Instant, or `null` if unknown / not applicable.
     *
     * @throws NetworkAuthResponseException If an error was encountered in the OAuth 2 flow when
     * using the Vonage Network Auth API to obtain the access token.
     *
     * @throws CamaraResponseException If the request was unsuccessful. This could be for the following reasons:
     * - **400**: Invalid request arguments.
     * - **401**: Request not authenticated due to missing, invalid, or expired credentials.
     * - **403**: Client does not have sufficient permissions to perform this action.
     * - **404**: SIM Swap can't be checked because the phone number is unknown.
     * - **409**: Another request is created for the same MSISDN.
     * - **502**: Bad gateway.
     */
    fun retrieveSimSwapDate(phoneNumber: String): Instant? =
        client.retrieveSimSwapDate(phoneNumber)
}
