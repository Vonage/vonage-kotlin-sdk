package com.vonage.client.kt

import com.fasterxml.jackson.databind.ObjectMapper
import com.marcinziolo.kotlin.wiremock.*
import com.vonage.client.messages.MessageRequest
import com.vonage.client.messages.viber.Category
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    val audioUrl = "https://example.com/audio.mp3"
    val videoUrl = "https://example.com/video.mp4"
    val fileUrl = "https://example.com/file.pdf"
    val captionMap = mapOf("caption" to caption)

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

    private fun audioBody(channel: String, additionalParams : Map<String, Any>? = null): Map<String, Any> =
        mediaBody(channel, "audio", audioUrl, additionalParams)

    private fun videoBody(channel: String, additionalParams : Map<String, Any>? = null): Map<String, Any> =
        mediaBody(channel, "video", videoUrl, additionalParams)

    private fun fileBody(channel: String, additionalParams : Map<String, Any>? = null): Map<String, Any> =
        mediaBody(channel, "file", fileUrl, additionalParams)


    @Test
    fun `send SMS text`() {
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

    @Test
    fun `send MMS audio`() {
        testSend(audioBody(mmsChannel, captionMap), mmsAudio {
            from(fromNumber); to(toNumber); url(audioUrl); caption(caption)
        })
    }

    @Test
    fun `send WhatsApp audio`() {
        testSend(audioBody(whatsappChannel), whatsappAudio {
            from(fromNumber); to(toNumber); url(audioUrl)
        })
    }

    @Test
    fun `send Messenger audio`() {
        testSend(audioBody(messengerChannel), messengerAudio {
            from(fromNumber); to(toNumber); url(audioUrl)
        })
    }

    @Test
    fun `send MMS video`() {
        testSend(videoBody(mmsChannel, captionMap), mmsVideo {
            from(fromNumber); to(toNumber); url(videoUrl); caption(caption)
        })
    }

    @Test
    fun `send WhatsApp video`() {
        testSend(videoBody(whatsappChannel, captionMap), whatsappVideo {
            from(fromNumber); to(toNumber); url(videoUrl); caption(caption)
        })
    }

    @Test
    fun `send Viber video`() {
        val duration = 23
        val fileSize = 7
        val ttl = 90
        val thumbUrl = "https://example.com/file1.jpg"
        testSend(videoBody(viberChannel,
            captionMap + mapOf("thumb_url" to thumbUrl)) + mapOf(viberChannel to mapOf(
                    "category" to "transaction",
                    "duration" to duration,
                    "file_size" to fileSize,
                    "ttl" to ttl
            )), viberVideo {
                from(fromNumber); to(toNumber); url(videoUrl); caption(caption);
                category(Category.TRANSACTION)
                duration(duration); ttl(ttl); fileSize(fileSize); thumbUrl(thumbUrl)
            }
        )
    }

    @Test
    fun `send Messenger video`() {
        testSend(videoBody(messengerChannel), messengerVideo {
            from(fromNumber); to(toNumber); url(videoUrl)
        })
    }

    @Test
    fun `send WhatsApp file`() {
        val fileName = "Document.pdf"
        testSend(fileBody(whatsappChannel, captionMap + mapOf("name" to fileName)), whatsappFile {
            from(fromNumber); to(toNumber); url(fileUrl); caption(caption); name(fileName)
        })
    }

    @Test
    fun `send Viber file`() {
        val fileName = "report.docx"
        testSend(fileBody(viberChannel, mapOf("name" to fileName)), viberFile {
            from(fromNumber); to(toNumber); url(fileUrl); name(fileName)
        })
    }

    @Test
    fun `send Messenger file`() {
        testSend(fileBody(messengerChannel), messengerFile {
            from(fromNumber); to(toNumber); url(fileUrl)
        })
    }

    @Test
    fun `send WhatsApp sticker`() {
        val stickerType = "sticker"
        val stickerUrl = "https://example.com/image.webp"
        testSend(mediaBody(whatsappChannel, stickerType, stickerUrl), whatsappSticker {
            from(fromNumber); to(toNumber); url(stickerUrl)
        })

        val stickerId = "aabb7a31-1d1f-4755-a574-2971d831cd5b"
        assertEquals(UUID.fromString(stickerId), whatsappSticker {
                from(fromNumber); to(toNumber); id(stickerId)
            }.sticker.id
        )

        assertThrows<IllegalStateException> { whatsappSticker {
            from(fromNumber); to(toNumber); id(stickerId); url(stickerUrl)
        } }
    }
}