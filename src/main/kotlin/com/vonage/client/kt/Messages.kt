package org.example.com.vonage.client.kt

import com.vonage.client.messages.sms.SmsTextRequest
import com.vonage.client.messages.whatsapp.WhatsappTextRequest

fun sms(init: SmsTextRequest.Builder.() -> Unit): SmsTextRequest {
    return SmsTextRequest.builder().apply(init).build()
}

fun whatsappText(init: WhatsappTextRequest.Builder.() -> Unit): WhatsappTextRequest {
    return WhatsappTextRequest.builder().apply(init).build()
}