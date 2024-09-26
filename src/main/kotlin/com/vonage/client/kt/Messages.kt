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

import com.vonage.client.ApiRegion
import com.vonage.client.messages.*
import com.vonage.client.messages.sms.*
import com.vonage.client.messages.mms.*
import com.vonage.client.messages.whatsapp.*
import com.vonage.client.messages.messenger.*
import com.vonage.client.messages.viber.*
import com.vonage.client.messages.rcs.*
import java.util.UUID

/**
 * Implementation of the [Messages API](https://developer.vonage.com/en/api/messages-olympus).
 *
 * *Authentication method:* JWT (recommended), API key & secret (limited functionality).
 */
class Messages internal constructor(private val client: MessagesClient) {

    /**
     * Send a message.
     *
     * The details of its format, channel, sender, recipient etc. are specified entirely by
     * the type and contents of the [MessageRequest]. This file contains utility functions for creating
     * each valid combination of [Channel] and [com.vonage.client.common.MessageType]. Generally, the only
     * required parameters are the `from` (sender), `to` (recipient), and the message content, which is
     * typically either `text` (for text messages) or `url` (for media messages).
     *
     * @param message The message to send. Use one of the DSL functions to create the message.
     * @param sandbox (OPTIONAL) Set to `true` to use the Messages API Sandbox endpoint.
     *
     * @return UUID of the message.
     *
     * @throws [MessageResponseException] If the message could not be sent. This may be for the following reasons:
     * - **401**: Invalid credentials.
     * - **402**: The account balance is too low.
     * - **422**: Malformed request.
     * - **429**: Too many requests.
     * - **500**: Internal server error.
     */
    fun send(message: MessageRequest, sandbox: Boolean = false): UUID =
        (if (sandbox) client.useSandboxEndpoint()
            else client.useRegularEndpoint()).sendMessage(message).messageUuid

    /**
     * Call this method to update an existing message.
     *
     * @param id The message UUID as a string.
     * @param region The API region server URL in which the message was sent.
     *
     * @return An [ExistingMessage] object for updating the existing message.
     */
    fun existingMessage(id: String, region: ApiRegion): ExistingMessage = ExistingMessage(id, region)

    /**
     * Class for working with an existing message.
     *
     * @property id The message ID.
     * @property region The regional URL server in which the message was sent.
     */
    inner class ExistingMessage internal constructor(id: String, val region: ApiRegion): ExistingResource(id) {

        /**
         * Marks the inbound message as read. Currently only applies to the WhatsApp channel.
         *
         * @throws [MessageResponseException] If the message could not be updated.
         * This may be for the following reasons:
         * - **401**: Invalid credentials.
         * - **404**: Not Found. The message ID is not known, or the wrong region was used.
         * - **422**: Malformed request.
         * - **429**: Too many requests.
         * - **500**: Internal server error.
         */
        fun markAsRead(): Unit = client.ackInboundMessage(id, region)

        /**
         * Revokes the outbound message. Currently only applies to the RCS channel.
         *
         * @throws [MessageResponseException] If the message could not be updated.
         * This may be for the following reasons:
         * - **401**: Invalid credentials.
         * - **404**: Not Found. The message ID is not known, or the wrong region was used.
         * - **422**: Malformed request.
         * - **429**: Too many requests.
         * - **500**: Internal server error.
         */
        fun revoke(): Unit = client.revokeOutboundMessage(id, region)
    }
}

/**
 * Create an SMS text message.
 * 
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [SmsTextRequest] object with the specified properties.
 */
fun smsText(properties: SmsTextRequest.Builder.() -> Unit): SmsTextRequest =
    SmsTextRequest.builder().apply(properties).build()

/**
 * Creates an MMS vCard message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return An [MmsVcardRequest] object with the specified properties.
 */
fun mmsVcard(properties: MmsVcardRequest.Builder.() -> Unit): MmsVcardRequest =
    MmsVcardRequest.builder().apply(properties).build()

/**
 * Creates an MMS image message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return An [MmsImageRequest] object with the specified properties.
 */
fun mmsImage(properties: MmsImageRequest.Builder.() -> Unit): MmsImageRequest =
    MmsImageRequest.builder().apply(properties).build()

/**
 * Creates an MMS audio message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return An [MmsAudioRequest] object with the specified properties.
 */
fun mmsAudio(properties: MmsAudioRequest.Builder.() -> Unit): MmsAudioRequest =
    MmsAudioRequest.builder().apply(properties).build()

/**
 * Creates an MMS video message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return An [MmsVideoRequest] object with the specified properties.
 */
fun mmsVideo(properties: MmsVideoRequest.Builder.() -> Unit): MmsVideoRequest =
    MmsVideoRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp text message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappTextRequest] object with the specified properties.
 */
fun whatsappText(properties: WhatsappTextRequest.Builder.() -> Unit): WhatsappTextRequest =
    WhatsappTextRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp image message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappImageRequest] object with the specified properties.
 */
fun whatsappImage(properties: WhatsappImageRequest.Builder.() -> Unit): WhatsappImageRequest =
    WhatsappImageRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp audio message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappAudioRequest] object with the specified properties.
 */
fun whatsappAudio(properties: WhatsappAudioRequest.Builder.() -> Unit): WhatsappAudioRequest =
    WhatsappAudioRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp video message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappVideoRequest] object with the specified properties.
 */
fun whatsappVideo(properties: WhatsappVideoRequest.Builder.() -> Unit): WhatsappVideoRequest =
    WhatsappVideoRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp file message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappFileRequest] object with the specified properties.
 */
fun whatsappFile(properties: WhatsappFileRequest.Builder.() -> Unit): WhatsappFileRequest =
    WhatsappFileRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp sticker message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappStickerRequest] object with the specified properties.
 */
fun whatsappSticker(properties: WhatsappStickerRequest.Builder.() -> Unit): WhatsappStickerRequest =
    WhatsappStickerRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp reaction message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappReactionRequest] object with the specified properties.
 */
fun whatsappReaction(properties: WhatsappReactionRequest.Builder.() -> Unit): WhatsappReactionRequest =
    WhatsappReactionRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp location message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappLocationRequest] object with the specified properties.
 */
fun whatsappLocation(properties: WhatsappLocationRequest.Builder.() -> Unit): WhatsappLocationRequest =
    WhatsappLocationRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp single product message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappSingleProductRequest] object with the specified properties.
 */
fun whatsappSingleProduct(properties: WhatsappSingleProductRequest.Builder.() -> Unit): WhatsappSingleProductRequest =
    WhatsappSingleProductRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp multi product message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappMultiProductRequest] object with the specified properties.
 */
fun whatsappMultiProduct(properties: WhatsappMultiProductRequest.Builder.() -> Unit): WhatsappMultiProductRequest =
    WhatsappMultiProductRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp template message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappTemplateRequest] object with the specified properties.
 */
fun whatsappTemplate(properties: WhatsappTemplateRequest.Builder.() -> Unit): WhatsappTemplateRequest =
    WhatsappTemplateRequest.builder().apply(properties).build()

/**
 * Creates a WhatsApp custom message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [WhatsappCustomRequest] object with the specified properties.
 */
fun whatsappCustom(properties: WhatsappCustomRequest.Builder.() -> Unit): WhatsappCustomRequest =
    WhatsappCustomRequest.builder().apply(properties).build()

/**
 * Creates a Messenger text message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [MessengerTextRequest] object with the specified properties.
 */
fun messengerText(properties: MessengerTextRequest.Builder.() -> Unit): MessengerTextRequest =
    MessengerTextRequest.builder().apply(properties).build()

/**
 * Creates a Messenger image message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [MessengerImageRequest] object with the specified properties.
 */
fun messengerImage(properties: MessengerImageRequest.Builder.() -> Unit): MessengerImageRequest =
    MessengerImageRequest.builder().apply(properties).build()

/**
 * Creates a Messenger audio message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [MessengerAudioRequest] object with the specified properties.
 */
fun messengerAudio(properties: MessengerAudioRequest.Builder.() -> Unit): MessengerAudioRequest =
    MessengerAudioRequest.builder().apply(properties).build()

/**
 * Creates a Messenger video message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [MessengerVideoRequest] object with the specified properties.
 */
fun messengerVideo(properties: MessengerVideoRequest.Builder.() -> Unit): MessengerVideoRequest =
    MessengerVideoRequest.builder().apply(properties).build()

/**
 * Creates a Messenger file message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [MessengerFileRequest] object with the specified properties.
 */
fun messengerFile(properties: MessengerFileRequest.Builder.() -> Unit): MessengerFileRequest =
    MessengerFileRequest.builder().apply(properties).build()

/**
 * Creates a Viber text message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [ViberTextRequest] object with the specified properties.
 */
fun viberText(properties: ViberTextRequest.Builder.() -> Unit): ViberTextRequest =
    ViberTextRequest.builder().apply(properties).build()

/**
 * Creates a Viber image message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [ViberImageRequest] object with the specified properties.
 */
fun viberImage(properties: ViberImageRequest.Builder.() -> Unit): ViberImageRequest =
    ViberImageRequest.builder().apply(properties).build()

/**
 * Creates a Viber video message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [ViberVideoRequest] object with the specified properties.
 */
fun viberVideo(properties: ViberVideoRequest.Builder.() -> Unit): ViberVideoRequest =
    ViberVideoRequest.builder().apply(properties).build()

/**
 * Creates a Viber file message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return A [ViberFileRequest] object with the specified properties.
 */
fun viberFile(properties: ViberFileRequest.Builder.() -> Unit): ViberFileRequest =
    ViberFileRequest.builder().apply(properties).build()

/**
 * Creates an RCS text message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return An [RcsTextRequest] object with the specified properties.
 */
fun rcsText(properties: RcsTextRequest.Builder.() -> Unit): RcsTextRequest =
    RcsTextRequest.builder().apply(properties).build()

/**
 * Creates an RCS image message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return An [RcsImageRequest] object with the specified properties.
 */
fun rcsImage(properties: RcsImageRequest.Builder.() -> Unit): RcsImageRequest =
    RcsImageRequest.builder().apply(properties).build()

/**
 * Creates an RCS video message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return An [RcsVideoRequest] object with the specified properties.
 */
fun rcsVideo(properties: RcsVideoRequest.Builder.() -> Unit): RcsVideoRequest =
    RcsVideoRequest.builder().apply(properties).build()

/**
 * Creates an RCS file message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return An [RcsFileRequest] object with the specified properties.
 */
fun rcsFile(properties: RcsFileRequest.Builder.() -> Unit): RcsFileRequest =
    RcsFileRequest.builder().apply(properties).build()

/**
 * Creates an RCS custom message.
 *
 * @param properties A lambda function for setting the message's parameters.
 *
 * @return An [RcsCustomRequest] object with the specified properties.
 */
fun rcsCustom(properties: RcsCustomRequest.Builder.() -> Unit): RcsCustomRequest =
    RcsCustomRequest.builder().apply(properties).build()
