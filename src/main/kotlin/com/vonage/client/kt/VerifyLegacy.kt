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

import com.vonage.client.verify.*

class VerifyLegacy internal constructor(private val client: VerifyClient) {

    fun verify(number: String, brand: String, properties: (VerifyRequest.Builder.() -> Unit) = {}): VerifyResponse =
        client.verify(VerifyRequest.builder(number, brand).apply(properties).build())

    fun psd2Verify(number: String, amount: Double, payee: String,
                   properties: (Psd2Request.Builder.() -> Unit) = {}): VerifyResponse =
        client.psd2Verify(Psd2Request.builder(number, amount, payee).apply(properties).build())

    fun search(vararg requestIds: String): SearchVerifyResponse = client.search(*requestIds)

    fun request(requestId: String): ExistingRequest = ExistingRequest(requestId)

    fun request(response: VerifyResponse): ExistingRequest = request(response.requestId)

    inner class ExistingRequest internal constructor(private val requestId: String) {

        fun cancel(): ControlResponse = client.cancelVerification(requestId)

        fun advance(): ControlResponse = client.advanceVerification(requestId)

        fun check(code: String): CheckResponse = client.check(requestId, code)

        fun search(): SearchVerifyResponse = client.search(requestId)

        @Override
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ExistingRequest
            return requestId == other.requestId
        }

        @Override
        override fun hashCode(): Int {
            return requestId.hashCode()
        }
    }

}