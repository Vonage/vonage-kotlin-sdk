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
