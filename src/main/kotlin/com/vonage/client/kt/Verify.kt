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

    fun cancelVerification(requestId: String) = verify2Client.cancelVerification(UUID.fromString(requestId))

    fun nextWorkflow(requestId: String) = verify2Client.nextWorkflow(UUID.fromString(requestId))

    fun checkVerificationCode(requestId: String, code: String) =
        verify2Client.checkVerificationCode(UUID.fromString(requestId), code)

    fun isValidVerificationCode(requestId: String, code: String): Boolean {
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
