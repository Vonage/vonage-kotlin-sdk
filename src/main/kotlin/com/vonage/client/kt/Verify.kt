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

import com.vonage.client.verify2.*
import java.util.*

/**
 * Implementation of the [Verify v2 API](https://developer.vonage.com/en/api/verify.v2).
 */
class Verify(private val client: Verify2Client) {

    fun sendVerification(
        brand: String = "Vonage",
        init: VerificationRequest.Builder.() -> Unit
    ): VerificationResponse = client.sendVerification(
        VerificationRequest.builder().brand(brand).apply(init).build()
    )

    inner class ExistingRequest internal constructor(private val uuid: UUID): ExistingResource(uuid.toString()) {

        fun cancel(): Unit = client.cancelVerification(uuid)

        fun nextWorkflow(): Unit = client.nextWorkflow(uuid)

        fun checkVerificationCode(code: String): VerifyCodeResponse =
            client.checkVerificationCode(uuid, code)

        fun isValidVerificationCode(code: String): Boolean {
            try {
                checkVerificationCode(code)
                return true
            } catch (ex: VerifyResponseException) {
                if (ex.statusCode == 400 || ex.statusCode == 410) {
                    return false
                } else {
                    throw ex
                }
            }
        }
    }

    fun request(requestId: UUID): ExistingRequest = ExistingRequest(requestId)

    fun request(requestId: String): ExistingRequest = request(UUID.fromString(requestId))
}

fun VerificationRequest.Builder.silentAuth(
        number: String, sandbox: Boolean? = null, redirectUrl: String? = null): VerificationRequest.Builder {
    val builder = SilentAuthWorkflow.builder(number)
    if (sandbox != null) builder.sandbox(sandbox)
    if (redirectUrl != null) builder.redirectUrl(redirectUrl)
    return addWorkflow(builder.build())
}

fun VerificationRequest.Builder.sms(
        number: String, init: SmsWorkflow.Builder.() -> Unit = {}): VerificationRequest.Builder =
    addWorkflow(SmsWorkflow.builder(number).apply(init).build())

fun VerificationRequest.Builder.voice(number: String): VerificationRequest.Builder =
    addWorkflow(VoiceWorkflow(number))

fun VerificationRequest.Builder.email(to: String, from: String? = null): VerificationRequest.Builder =
    addWorkflow(EmailWorkflow(to, from))

fun VerificationRequest.Builder.whatsapp(to: String, from: String): VerificationRequest.Builder =
    addWorkflow(WhatsappWorkflow(to, from))

fun VerificationRequest.Builder.whatsappCodeless(to: String, from: String): VerificationRequest.Builder =
    addWorkflow(WhatsappCodelessWorkflow(to, from))
