package com.vonage.client.kt

import com.fasterxml.jackson.databind.ObjectMapper
import com.marcinziolo.kotlin.wiremock.*
import com.vonage.client.messages.MessageRequest
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class MessagesTest : AbstractTest() {
    val messagesClient = vonageClient.messages
    val messageUuid = UUID.fromString("aaaaaaaa-bbbb-4ccc-8ddd-0123456789ab")
    val mmsChannel = "mms"
    val whatsappChannel = "whatsapp"
    val viberChannel = "viber_service"
    val messengerChannel = "messenger"
    val fromNumber = "447700900001"
    val toNumber = "447712345689"
    val text = "Hello, World!"
    val caption = "Additional text to accompany the media"
    val imageUrl = "https://example.com/image.jpg"
    val captionMap = mutableMapOf("caption" to caption)

    private fun mockResponse(expectedBodyParams: Map<String, Any>) {
        wiremock.post {
            url equalTo "/v1/messages"
            headers contains "User-Agent" like "vonage-java-sdk.*"
            headers contains "Authorization" like "Bearer eyJ.+"
            headers contains "Content-Type" equalTo "application/json"
            headers contains "Accept" equalTo "application/json"
            body equalTo ObjectMapper().writeValueAsString(expectedBodyParams)
        } returns {
            header = "Content-Type" to "application/json"
            statusCode = 202
            body = """
            {
                "message_uuid": "$messageUuid"
            }
            """
        }
    }

    private fun testSend(expectedBodyParams: Map<String, Any>, req: MessageRequest) {
        mockResponse(expectedBodyParams)
        assertEquals(messageUuid, messagesClient.send(req))
    }

    private fun baseBody(messageType: String, channel: String): Map<String, Any> =
        mapOf(
            "message_type" to messageType,
            "to" to toNumber,
            "from" to fromNumber,
            "channel" to channel
        )

    private fun textBody(channel: String): Map<String, Any> =
        baseBody("text", channel) + mapOf("text" to text)

    private fun mediaBody(channel: String, messageType: String, url: String, additionalParams: Map<String, Any>? = null): Map<String, Any> =
        baseBody(messageType, channel) + mapOf(messageType to mapOf("url" to url) + (additionalParams ?: mapOf()))

    private fun imageBody(channel: String, additionalParams : Map<String, Any>? = null): Map<String, Any> =
        mediaBody(channel, "image", imageUrl, additionalParams)

    @Test
    fun `send SMS`() {
        testSend(textBody("sms"), smsText {
            from(fromNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send WhatsApp text`() {
        testSend(textBody(whatsappChannel), whatsappText {
            from(fromNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send Viber text`() {
        testSend(textBody(viberChannel), viberText {
            from(fromNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send Messenger text`() {
        testSend(textBody(messengerChannel), messengerText {
            from(fromNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send MMS vCard`() {
        val vcardUrl = "https://example.com/conatact.vcf"
        testSend(mediaBody(mmsChannel, "vcard", vcardUrl, captionMap), mmsVcard {
            from(fromNumber); to(toNumber); url(vcardUrl); caption(caption)
        })
    }

    @Test
    fun `send MMS image`() {
        testSend(imageBody(mmsChannel, captionMap), mmsImage {
            from(fromNumber); to(toNumber); url(imageUrl); caption(caption)
        })
    }

    @Test
    fun `send WhatsApp image`() {
        testSend(imageBody(whatsappChannel, captionMap), whatsappImage {
            from(fromNumber); to(toNumber); url(imageUrl); caption(caption)
        })
    }

    @Test
    fun `send Viber image`() {
        testSend(imageBody(viberChannel), viberImage {
            from(fromNumber); to(toNumber); url(imageUrl)
        })
    }

    @Test
    fun `send Messenger image`() {
        testSend(imageBody(messengerChannel), messengerImage {
            from(fromNumber); to(toNumber); url(imageUrl)
        })
    }
}