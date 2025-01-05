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

import com.vonage.client.verify.*

/**
 * Implementation of the [Verify v1 API](https://developer.vonage.com/en/api/verify).
 *
 * *Authentication method:* API key & secret or signature secret.
 */
class VerifyLegacy internal constructor(private val client: VerifyClient) {

    /**
     * Initiate a verification request.
     *
     * @param number The phone number to verify in E.164 format.
     *
     * @param brand The name of the company or app sending the verification request.
     * This is what the user will see the sender as on their device.
     *
     * @param properties A lambda function for specifying additional parameters of the request.
     *
     * @return A [VerifyResponse] object containing the request ID and status.
     */
    fun verify(number: String, brand: String, properties: (VerifyRequest.Builder.() -> Unit) = {}): VerifyResponse =
        client.verify(VerifyRequest.builder(number, brand).apply(properties).build())

    /**
     * Generate and send a PIN to your user to authorize a payment, compliant with Payment Services Directive 2.
     *
     * @param number The phone number to send the request to, in E.164 format.
     *
     * @param amount The decimal amount of the payment to be confirmed, in Euros.
     *
     * @param payee Name of the recipient that the user is confirming a payment to.
     *
     * @param properties (OPTIONAL) A lambda function for specifying additional parameters of the request.
     *
     * @return A [VerifyResponse] object containing the request ID and status.
     */
    fun psd2Verify(number: String, amount: Double, payee: String,
                   properties: (Psd2Request.Builder.() -> Unit) = {}): VerifyResponse =
        client.psd2Verify(Psd2Request.builder(number, amount, payee).apply(properties).build())

    /**
     * Use this method to check the status of past or current verification requests.
     *
     * @param requestIds One or more request IDs to check.
     *
     * @return Information about the verification request(s).
     *
     * @see [ExistingRequest.info] An alias for this method.
     */
    fun search(vararg requestIds: String): SearchVerifyResponse = client.search(*requestIds)

    /**
     * Call this method to work with an existing verification request.
     *
     * @param response Response object containing the request ID.
     */
    fun request(response: VerifyResponse): ExistingRequest = request(response.requestId)

    /**
     * Call this method to work with an existing verification request.
     *
     * @param requestId ID of the verification request to work with.
     */
    fun request(requestId: String): ExistingRequest = ExistingRequest(requestId)

    /**
     * Call this method to work with an existing verification request.
     *
     * @param id ID of the verification request to work with.
     */
    inner class ExistingRequest internal constructor(id: String): ExistingResource(id) {

        /**
         * Retrieve details about the verification request.
         *
         * @return Information about the verification request.
         *
         * @see [search] An alias for this method.
         */
        fun info(): SearchVerifyResponse = client.search(id)

        /**
         * Cancel the verification request.
         */
        fun cancel(): ControlResponse = client.cancelVerification(id)

        /**
         * Trigger the next verification step.
         */
        fun advance(): ControlResponse = client.advanceVerification(id)

        /**
         * Check the verification code.
         *
         * @param code PIN code to check, as entered by the user.
         */
        fun check(code: String): CheckResponse = client.check(id, code)
    }
}