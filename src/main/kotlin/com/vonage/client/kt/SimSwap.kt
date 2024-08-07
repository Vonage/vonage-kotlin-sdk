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

import com.vonage.client.camara.simswap.*
import java.time.Instant

class SimSwap internal constructor(private val client: SimSwapClient) {

    fun checkSimSwap(phoneNumber: String, maxAgeHours: Int = 240): Boolean =
        client.checkSimSwap(phoneNumber, maxAgeHours)

    fun retrieveSimSwapDate(phoneNumber: String): Instant? =
        client.retrieveSimSwapDate(phoneNumber)
}
