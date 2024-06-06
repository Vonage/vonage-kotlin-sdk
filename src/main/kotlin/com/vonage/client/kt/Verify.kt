package com.vonage.client.kt

import com.vonage.client.verify2.*
import java.util.*

class Verify(private val verify2Client: Verify2Client) {

    fun sendVerification(
        brand: String = "Vonage",
        init: VerificationRequest.Builder.() -> Unit
    ): UUID {

        return verify2Client.sendVerification(
            VerificationRequest.builder().brand(brand).apply(init).build()
        ).requestId
    }

    fun cancelVerification(requestId: String) {
        verify2Client.cancelVerification(UUID.fromString(requestId))
    }

    fun nextWorkflow(requestId: String) {
        verify2Client.nextWorkflow(UUID.fromString(requestId))
    }

    fun checkVerificationCode(requestId: String, code: String) {
        verify2Client.checkVerificationCode(UUID.fromString(requestId), code)
    }

    fun isValidVerificationCode(requestId: String, code: String): Boolean {
        try {
            verify2Client.checkVerificationCode(UUID.fromString(requestId), code)
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

fun VerificationRequest.Builder.silentAuth(number: String): VerificationRequest.Builder {
    return this.addWorkflow(SilentAuthWorkflow(number))
}

fun VerificationRequest.Builder.sms(
    number: String,
    init: SmsWorkflow.Builder.() -> Unit = {}
): VerificationRequest.Builder {
    return this.addWorkflow(SmsWorkflow.builder(number).apply(init).build())
}

fun VerificationRequest.Builder.voice(number: String): VerificationRequest.Builder {
    return this.addWorkflow(VoiceWorkflow(number))
}

fun VerificationRequest.Builder.email(to: String, from: String? = null): VerificationRequest.Builder {
    return this.addWorkflow(EmailWorkflow(to, from))
}

fun VerificationRequest.Builder.whatsapp(to: String, from: String): VerificationRequest.Builder {
    return this.addWorkflow(WhatsappWorkflow(to, from))
}

fun VerificationRequest.Builder.whatsappCodeless(to: String, from: String): VerificationRequest.Builder {
    return this.addWorkflow(WhatsappCodelessWorkflow(to, from))
}
