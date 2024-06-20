package com.vonage.client.kt

import com.marcinziolo.kotlin.wiremock.*
import com.vonage.client.messages.MessageRequest
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class MessagesTest : AbstractTest() {
    val messagesClient = vonageClient.messages
    val fromNumber = "447700900001"
    val toNumber = "447712345689"
    val text = "Hello, World!"
    val messageUuid = UUID.fromString("aaaaaaaa-bbbb-4ccc-8ddd-0123456789ab")

    private fun mockResponse(expectedBodyParams: Map<String, String>) {
        wiremock.post {
            url equalTo "/v1/messages"
            headers contains "User-Agent" like "vonage-java-sdk.*"
            headers contains "Authorization" like "Bearer eyJ.+"
            headers contains "Content-Type" equalTo "application/json"
            headers contains "Accept" equalTo "application/json"
            for (entry in expectedBodyParams.entries) {
                body contains entry.key equalTo entry.value
            }
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

    private fun testSend(expectedBodyParams: Map<String, String>, req: MessageRequest) {
        mockResponse(expectedBodyParams)
        assertEquals(messageUuid, messagesClient.send(req))
    }

    private fun expectedTextBody(channel: String): Map<String, String> = mapOf(
        "message_type" to "text",
        "text" to text,
        "to" to toNumber,
        "from" to fromNumber,
        "channel" to channel
    )

    @Test
    fun `send SMS`() {
        testSend(expectedTextBody("sms"), smsText {
            from(fromNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send WhatsApp text`() {
        testSend(expectedTextBody("whatsapp"), whatsappText {
            from(fromNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send Viber text`() {
        testSend(expectedTextBody("viber_service"), viberText {
            from(fromNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send Messenger text`() {
        testSend(expectedTextBody("messenger"), messengerText {
            from(fromNumber); to(toNumber); text(text)
        })
    }
}