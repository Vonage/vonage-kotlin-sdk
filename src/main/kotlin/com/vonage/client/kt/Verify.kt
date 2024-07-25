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

class Verify(private val verify2Client: Verify2Client) {

    fun sendVerification(
        brand: String = "Vonage",
        init: VerificationRequest.Builder.() -> Unit
    ): VerificationResponse = verify2Client.sendVerification(
        VerificationRequest.builder().brand(brand).apply(init).build()
    )

    fun cancelVerification(requestId: UUID) = verify2Client.cancelVerification(requestId)

    fun cancelVerification(requestId: String) = cancelVerification(UUID.fromString(requestId))

    fun nextWorkflow(requestId: UUID) = verify2Client.nextWorkflow(requestId)

    fun nextWorkflow(requestId: String) = nextWorkflow(UUID.fromString(requestId))

    fun checkVerificationCode(requestId: UUID, code: String) =
        verify2Client.checkVerificationCode(requestId, code)

    fun checkVerificationCode(requestId: String, code: String) =
        checkVerificationCode(UUID.fromString(requestId), code)

    fun isValidVerificationCode(requestId: String, code: String): Boolean =
        isValidVerificationCode(UUID.fromString(requestId), code)

    fun isValidVerificationCode(requestId: UUID, code: String): Boolean {
        try {
            checkVerificationCode(requestId, code)
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
