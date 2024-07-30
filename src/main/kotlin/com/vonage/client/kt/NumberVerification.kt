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
import java.net.URI

class NumberVerification(private val nvClient: NumberVerificationClient) {
    private var redirectUri: URI? = null

    fun createVerificationUrl(phoneNumber: String, redirectUrl: String, state: String? = null): URI {
        redirectUri = URI.create(redirectUrl)
        return nvClient.initiateVerification(phoneNumber, redirectUri, state)
    }

    fun verifyNumberWithCode(phoneNumber: String, code: String, redirectUrl: String? = null): Boolean {
        if (redirectUrl != null) redirectUri = URI.create(redirectUrl)
        return nvClient.verifyNumber(phoneNumber, redirectUri, code)
    }
}
