package org.example.com.vonage.client.kt

import com.vonage.client.verify2.SmsWorkflow
import com.vonage.client.verify2.VerificationRequest
import com.vonage.client.verify2.Verify2Client
import com.vonage.client.verify2.VoiceWorkflow
import java.util.*

fun Verify2Client.sendVerification(init: VerificationRequest.Builder.() -> Unit) : UUID {
    return this.sendVerification(VerificationRequest.builder().apply(init).build()).requestId
}

fun VerificationRequest.Builder.sms(number: String, init: SmsWorkflow.Builder.() -> Unit = {}) : VerificationRequest.Builder {
    return this.addWorkflow(SmsWorkflow.builder(number).apply(init).build())
}

fun VerificationRequest.Builder.voice(number: String) : VerificationRequest.Builder {
    return this.addWorkflow(VoiceWorkflow(number))
}