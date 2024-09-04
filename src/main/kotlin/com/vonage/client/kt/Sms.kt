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

import com.vonage.client.sms.*
import com.vonage.client.sms.messages.*

/**
 * Implementation of the [SMS API](https://developer.vonage.com/en/api/sms).
 *
 * Authentication method: API key & secret or signature secret.
 */
class Sms internal constructor(private val client: SmsClient) {

    private fun send(msgObj: Message, statusReport: Boolean?, ttl: Int?,
                     messageClass: Message.MessageClass?, clientRef: String?,
                     contentId: String?, entityId: String?,
                     callbackUrl: String?): List<SmsSubmissionResponseMessage> {
        if (statusReport != null) msgObj.statusReportRequired = statusReport
        msgObj.timeToLive = ttl?.toLong()
        msgObj.messageClass = messageClass
        if (clientRef != null) msgObj.clientReference = clientRef
        msgObj.contentId = contentId
        msgObj.entityId = entityId
        msgObj.callbackUrl = callbackUrl
        return client.submitMessage(msgObj).messages
    }

    /**
     * Send a text message.
     *
     * @param from The sender ID. This can be a phone number or a short alphanumeric string.
     *
     * @param to The recipient phone number in E.164 format.
     *
     * @param message Text of the message to send.
     *
     * @param unicode `true` if the message should be sent as Unicode, `false` for GSM (the default).
     *
     * @param statusReport (OPTIONAL) Whether to include a Delivery Receipt.
     *
     * @param ttl (OPTIONAL) The duration in milliseconds the delivery of an SMS will be attempted. By default, Vonage
     * attempts delivery for 72 hours, however the maximum effective value depends on the operator and is typically
     * 24 - 48 hours. We recommend this value should be kept at its default or at least 30 minutes.
     *
     * @param messageClass (OPTIONAL) Data Coding Scheme value of the message.
     *
     * @param clientRef (OPTIONAL) You can optionally include your own reference of up to 100 characters.
     *
     * @param contentId (OPTIONAL) This is to satisfy regulatory requirements when sending an SMS to specific countries.
     *
     * @param entityId (OPTIONAL) This is to satisfy regulatory requirements when sending an SMS to specific countries.
     *
     * @param callbackUrl (OPTIONAL) The URL to which delivery receipts for this message are sent.
     *
     * @return A list of [SmsSubmissionResponseMessage] objects, one for each message part sent.
     * Multiple messages are sent if the text was too long. For convenience, you can use the
     * [wasSuccessfullySent] method to check if all messages were sent successfully.
     */
    fun sendText(from: String, to: String, message: String, unicode: Boolean = false,
                 statusReport: Boolean? = null, ttl: Int? = null,
                 messageClass: Message.MessageClass? = null, clientRef: String? = null,
                 contentId: String? = null, entityId: String? = null,
                 callbackUrl: String? = null): List<SmsSubmissionResponseMessage> =
        send(
            TextMessage(from, to, message, unicode),
            statusReport, ttl, messageClass, clientRef, contentId, entityId, callbackUrl
        )

    /**
     * Send a binary (hex) message.
     *
     * @param from The sender ID. This can be a phone number or a short alphanumeric string.
     *
     * @param to The recipient phone number in E.164 format.
     *
     * @param body Hex encoded binary data message to send.
     *
     * @param udh Hex encoded User Data Header.
     *
     * @param protocolId (OPTIONAL) The value of the protocol identifier to use. Should be aligned with `udh`.
     *
     * @param statusReport (OPTIONAL) Whether to include a Delivery Receipt.
     *
     * @param ttl (OPTIONAL) The duration in milliseconds the delivery of an SMS will be attempted. By default, Vonage
     * attempts delivery for 72 hours, however the maximum effective value depends on the operator and is typically
     * 24 - 48 hours. We recommend this value should be kept at its default or at least 30 minutes.
     *
     * @param messageClass (OPTIONAL) Data Coding Scheme value of the message.
     *
     * @param clientRef (OPTIONAL) You can optionally include your own reference of up to 100 characters.
     *
     * @param contentId (OPTIONAL) This is to satisfy regulatory requirements when sending an SMS to specific countries.
     *
     * @param entityId (OPTIONAL) This is to satisfy regulatory requirements when sending an SMS to specific countries.
     *
     * @param callbackUrl (OPTIONAL) The URL to which delivery receipts for this message are sent.
     *
     * @return A list of [SmsSubmissionResponseMessage] objects, one for each message part sent.
     * Multiple messages are sent if the body was too long. For convenience, you can use the
     * [wasSuccessfullySent] method to check if all messages were sent successfully.
     */
    fun sendBinary(from: String, to: String, body: ByteArray, udh: ByteArray,
                   protocolId: Int? = null, statusReport: Boolean? = null, ttl: Int? = null,
                   messageClass: Message.MessageClass? = null, clientRef: String? = null,
                   contentId: String? = null, entityId: String? = null, callbackUrl: String? = null
                   ): List<SmsSubmissionResponseMessage> {
        val msgObj = BinaryMessage(from, to, body, udh)
        if (protocolId != null) msgObj.protocolId = protocolId
        return send(msgObj, statusReport, ttl, messageClass, clientRef, contentId, entityId, callbackUrl)
    }

    /**
     * Convenience method to check if all messages were sent successfully.
     *
     * @param response The list of [SmsSubmissionResponseMessage] objects returned when sending a message.
     *
     * @return `true` if all messages responses have a status of [MessageStatus.OK], `false` otherwise.
     */
    fun wasSuccessfullySent(response: List<SmsSubmissionResponseMessage>): Boolean =
        response.all { ssrm -> ssrm.status == MessageStatus.OK }
}
