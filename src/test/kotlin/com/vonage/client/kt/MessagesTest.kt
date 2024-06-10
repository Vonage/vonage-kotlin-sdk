package com.vonage.client.kt

import com.marcinziolo.kotlin.wiremock.*
import org.junit.jupiter.api.Test

class MessagesTest : AbstractTest() {
    val messagesClient = vonageClient.messages
    val fromNumber = "447700900001"
    val toNumber = "447712345689"
    val text = "Hello, World!"

    fun expectedTextBody(channel: String): Map<String, String> = mapOf(
        "message_type" to "text",
        "text" to text,
        "to" to toNumber,
        "from" to fromNumber,
        "channel" to channel
    )

    fun mockResponse(expectedBodyParams: Map<String, String>) {
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
                "message_uuid": "aaaaaaaa-bbbb-4ccc-8ddd-0123456789ab"
            }
            """
        }
    }

    @Test
    fun `send SMS`() {
        mockResponse(expectedTextBody("sms"))
        messagesClient.send(smsText {
            from(fromNumber)
            to(toNumber)
            text(text)
        })
    }
}