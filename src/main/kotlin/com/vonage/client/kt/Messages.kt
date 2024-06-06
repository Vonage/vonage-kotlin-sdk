package com.vonage.client.kt

import com.vonage.client.messages.*
import com.vonage.client.messages.sms.*
import com.vonage.client.messages.mms.*
import com.vonage.client.messages.whatsapp.*
import com.vonage.client.messages.messenger.*
import com.vonage.client.messages.viber.*
import java.util.UUID

class Messages(private val client: MessagesClient) {
    fun send(message: MessageRequest): UUID {
        return client.sendMessage(message).messageUuid
    }
}

fun smsText(init: SmsTextRequest.Builder.() -> Unit): SmsTextRequest {
    return SmsTextRequest.builder().apply(init).build()
}

fun mmsVcard(init: MmsVcardRequest.Builder.() -> Unit): MmsVcardRequest {
    return MmsVcardRequest.builder().apply(init).build()
}

fun mmsImage(init: MmsImageRequest.Builder.() -> Unit): MmsImageRequest {
    return MmsImageRequest.builder().apply(init).build()
}

fun mmsAudio(init: MmsAudioRequest.Builder.() -> Unit): MmsAudioRequest {
    return MmsAudioRequest.builder().apply(init).build()
}

fun mmsVideo(init: MmsVideoRequest.Builder.() -> Unit): MmsVideoRequest {
    return MmsVideoRequest.builder().apply(init).build()
}

fun whatsappText(init: WhatsappTextRequest.Builder.() -> Unit): WhatsappTextRequest {
    return WhatsappTextRequest.builder().apply(init).build()
}

fun whatsappImage(init: WhatsappImageRequest.Builder.() -> Unit): WhatsappImageRequest {
    return WhatsappImageRequest.builder().apply(init).build()
}

fun whatsappAudio(init: WhatsappAudioRequest.Builder.() -> Unit): WhatsappAudioRequest {
    return WhatsappAudioRequest.builder().apply(init).build()
}

fun whatsappVideo(init: WhatsappVideoRequest.Builder.() -> Unit): WhatsappVideoRequest {
    return WhatsappVideoRequest.builder().apply(init).build()
}

fun whatsappFile(init: WhatsappFileRequest.Builder.() -> Unit): WhatsappFileRequest {
    return WhatsappFileRequest.builder().apply(init).build()
}

fun whatsappSticker(init: WhatsappStickerRequest.Builder.() -> Unit): WhatsappStickerRequest {
    return WhatsappStickerRequest.builder().apply(init).build()
}

fun whatsappLocation(init: WhatsappLocationRequest.Builder.() -> Unit): WhatsappLocationRequest {
    return WhatsappLocationRequest.builder().apply(init).build()
}

fun whatsappSingleProduct(init: WhatsappSingleProductRequest.Builder.() -> Unit): WhatsappSingleProductRequest {
    return WhatsappSingleProductRequest.builder().apply(init).build()
}

fun whatsappMultiProduct(init: WhatsappMultiProductRequest.Builder.() -> Unit): WhatsappMultiProductRequest {
    return WhatsappMultiProductRequest.builder().apply(init).build()
}

fun whatsappTemplate(init: WhatsappTemplateRequest.Builder.() -> Unit): WhatsappTemplateRequest {
    return WhatsappTemplateRequest.builder().apply(init).build()
}

fun messengerText(init: MessengerTextRequest.Builder.() -> Unit): MessengerTextRequest {
    return MessengerTextRequest.builder().apply(init).build()
}

fun messengerImage(init: MessengerImageRequest.Builder.() -> Unit): MessengerImageRequest {
    return MessengerImageRequest.builder().apply(init).build()
}

fun messengerAudio(init: MessengerAudioRequest.Builder.() -> Unit): MessengerAudioRequest {
    return MessengerAudioRequest.builder().apply(init).build()
}

fun messengerVideo(init: MessengerVideoRequest.Builder.() -> Unit): MessengerVideoRequest {
    return MessengerVideoRequest.builder().apply(init).build()
}

fun messengerFile(init: MessengerFileRequest.Builder.() -> Unit): MessengerFileRequest {
    return MessengerFileRequest.builder().apply(init).build()
}

fun viberText(init: ViberTextRequest.Builder.() -> Unit): ViberTextRequest {
    return ViberTextRequest.builder().apply(init).build()
}

fun viberImage(init: ViberImageRequest.Builder.() -> Unit): ViberImageRequest {
    return ViberImageRequest.builder().apply(init).build()
}

fun viberVideo(init: ViberVideoRequest.Builder.() -> Unit): ViberVideoRequest {
    return ViberVideoRequest.builder().apply(init).build()
}

fun viberFile(init: ViberFileRequest.Builder.() -> Unit): ViberFileRequest {
    return ViberFileRequest.builder().apply(init).build()
}