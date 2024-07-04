package com.vonage.client.kt

import com.vonage.client.messages.InboundMessage
import com.vonage.client.sms.*
import com.vonage.client.sms.messages.*

class SMS(private val smsClient: SmsClient) {

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
        return smsClient.submitMessage(msgObj).messages
    }

    fun sendText(from: String, to: String, message: String, unicode: Boolean = false,
                 statusReport: Boolean? = null, ttl: Int? = null,
                 messageClass: Message.MessageClass? = null, clientRef: String? = null,
                 contentId: String? = null, entityId: String? = null,
                 callbackUrl: String? = null): List<SmsSubmissionResponseMessage> =
        send(
            TextMessage(from, to, message, unicode),
            statusReport, ttl, messageClass, clientRef, contentId, entityId, callbackUrl
        )

    fun sendBinary(from: String, to: String, body: ByteArray, udh: ByteArray,
                   protocolId: Int? = null, statusReport: Boolean? = null, ttl: Int? = null,
                   messageClass: Message.MessageClass? = null, clientRef: String? = null,
                   contentId: String? = null, entityId: String? = null, callbackUrl: String? = null
                   ): List<SmsSubmissionResponseMessage> {
        val msgObj = BinaryMessage(from, to, body, udh)
        if (protocolId != null) msgObj.protocolId = protocolId
        return send(msgObj, statusReport, ttl, messageClass, clientRef, contentId, entityId, callbackUrl)
    }

    fun wasSuccessfullySent(response: List<SmsSubmissionResponseMessage>): Boolean =
        response.all { ssrm -> ssrm.status == MessageStatus.OK }
}
