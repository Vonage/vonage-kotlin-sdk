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

import com.vonage.client.messages.*
import com.vonage.client.messages.sms.*
import com.vonage.client.messages.mms.*
import com.vonage.client.messages.whatsapp.*
import com.vonage.client.messages.messenger.*
import com.vonage.client.messages.viber.*
import java.util.UUID

/**
 * Implementation of the [Messages API](https://developer.vonage.com/en/api/messages-olympus).
 *
 * Authentication method: JWT (recommended), API key & secret (limited functionality).
 */
class Messages internal constructor(private val client: MessagesClient) {
    fun send(message: MessageRequest, sandbox: Boolean = false): UUID =
        (if (sandbox) client.useSandboxEndpoint()
            else client.useRegularEndpoint()).sendMessage(message).messageUuid
}

fun smsText(init: SmsTextRequest.Builder.() -> Unit): SmsTextRequest =
    SmsTextRequest.builder().apply(init).build()

fun mmsVcard(init: MmsVcardRequest.Builder.() -> Unit): MmsVcardRequest =
    MmsVcardRequest.builder().apply(init).build()

fun mmsImage(init: MmsImageRequest.Builder.() -> Unit): MmsImageRequest =
    MmsImageRequest.builder().apply(init).build()

fun mmsAudio(init: MmsAudioRequest.Builder.() -> Unit): MmsAudioRequest =
    MmsAudioRequest.builder().apply(init).build()

fun mmsVideo(init: MmsVideoRequest.Builder.() -> Unit): MmsVideoRequest =
    MmsVideoRequest.builder().apply(init).build()

fun whatsappText(init: WhatsappTextRequest.Builder.() -> Unit): WhatsappTextRequest =
    WhatsappTextRequest.builder().apply(init).build()

fun whatsappImage(init: WhatsappImageRequest.Builder.() -> Unit): WhatsappImageRequest =
    WhatsappImageRequest.builder().apply(init).build()

fun whatsappAudio(init: WhatsappAudioRequest.Builder.() -> Unit): WhatsappAudioRequest =
    WhatsappAudioRequest.builder().apply(init).build()

fun whatsappVideo(init: WhatsappVideoRequest.Builder.() -> Unit): WhatsappVideoRequest =
    WhatsappVideoRequest.builder().apply(init).build()

fun whatsappFile(init: WhatsappFileRequest.Builder.() -> Unit): WhatsappFileRequest =
    WhatsappFileRequest.builder().apply(init).build()

fun whatsappSticker(init: WhatsappStickerRequest.Builder.() -> Unit): WhatsappStickerRequest =
    WhatsappStickerRequest.builder().apply(init).build()

fun whatsappLocation(init: WhatsappLocationRequest.Builder.() -> Unit): WhatsappLocationRequest =
    WhatsappLocationRequest.builder().apply(init).build()

fun whatsappSingleProduct(init: WhatsappSingleProductRequest.Builder.() -> Unit): WhatsappSingleProductRequest =
    WhatsappSingleProductRequest.builder().apply(init).build()

fun whatsappMultiProduct(init: WhatsappMultiProductRequest.Builder.() -> Unit): WhatsappMultiProductRequest =
    WhatsappMultiProductRequest.builder().apply(init).build()

fun whatsappTemplate(init: WhatsappTemplateRequest.Builder.() -> Unit): WhatsappTemplateRequest =
    WhatsappTemplateRequest.builder().apply(init).build()

fun whatsappCustom(init: WhatsappCustomRequest.Builder.() -> Unit): WhatsappCustomRequest =
    WhatsappCustomRequest.builder().apply(init).build()

fun messengerText(init: MessengerTextRequest.Builder.() -> Unit): MessengerTextRequest =
    MessengerTextRequest.builder().apply(init).build()

fun messengerImage(init: MessengerImageRequest.Builder.() -> Unit): MessengerImageRequest =
    MessengerImageRequest.builder().apply(init).build()

fun messengerAudio(init: MessengerAudioRequest.Builder.() -> Unit): MessengerAudioRequest =
    MessengerAudioRequest.builder().apply(init).build()

fun messengerVideo(init: MessengerVideoRequest.Builder.() -> Unit): MessengerVideoRequest =
    MessengerVideoRequest.builder().apply(init).build()

fun messengerFile(init: MessengerFileRequest.Builder.() -> Unit): MessengerFileRequest =
    MessengerFileRequest.builder().apply(init).build()

fun viberText(init: ViberTextRequest.Builder.() -> Unit): ViberTextRequest =
    ViberTextRequest.builder().apply(init).build()

fun viberImage(init: ViberImageRequest.Builder.() -> Unit): ViberImageRequest =
    ViberImageRequest.builder().apply(init).build()

fun viberVideo(init: ViberVideoRequest.Builder.() -> Unit): ViberVideoRequest =
    ViberVideoRequest.builder().apply(init).build()

fun viberFile(init: ViberFileRequest.Builder.() -> Unit): ViberFileRequest =
    ViberFileRequest.builder().apply(init).build()