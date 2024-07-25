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

import com.vonage.client.common.HttpMethod
import com.vonage.client.messages.*
import com.vonage.client.messages.viber.Category
import com.vonage.client.messages.whatsapp.Locale
import com.vonage.client.messages.whatsapp.Policy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MessagesTest : AbstractTest() {
    private val messagesClient = vonage.messages
    private val sendUrl = "/v1/messages"
    private val messageUuid = testUuid
    private val mmsChannel = "mms"
    private val whatsappChannel = "whatsapp"
    private val viberChannel = "viber_service"
    private val messengerChannel = "messenger"
    private val caption = "Additional text to accompany the media"
    private val imageUrl = "https://example.com/image.jpg"
    private val audioUrl = "https://example.com/audio.mp3"
    private val videoUrl = "https://example.com/video.mp4"
    private val fileUrl = "https://example.com/file.pdf"
    private val captionMap = mapOf("caption" to caption)


    private fun testSend(expectedBodyParams: Map<String, Any>, req: MessageRequest) {
        val status = 202
        val expectedResponseParams = mapOf("message_uuid" to messageUuid)

        mockPost(
            expectedUrl = sendUrl, expectedRequestParams = expectedBodyParams,
            status = status, expectedResponseParams = expectedResponseParams
        )
        assertEquals(messageUuid, messagesClient.send(req))

        // TODO fix mocking full url
        //val sandboxUrl = "https://messages-sandbox.nexmo.com$sendUrl"
        //mockJsonJwtPost(sandboxUrl, expectedBodyParams, status, expectedResponseParams)
        //assertEquals(messageUuid, messagesClient.sendSandbox(req))
    }

    private fun baseBody(messageType: String, channel: String): Map<String, Any> =
        mapOf(
            "message_type" to messageType,
            "to" to toNumber,
            "from" to altNumber,
            "channel" to channel
        )

    private fun textBody(channel: String, additionalParams: Map<String, Any> = mapOf()): Map<String, Any> =
        baseBody("text", channel) + mapOf("text" to text) + additionalParams

    private fun mediaBody(channel: String, messageType: String, url: String,
                          additionalParams: Map<String, Any>? = null): Map<String, Any> =
        baseBody(messageType, channel) + mapOf(messageType to mapOf("url" to url) + (additionalParams ?: mapOf()))

    private fun imageBody(channel: String, additionalParams : Map<String, Any>? = null): Map<String, Any> =
        mediaBody(channel, "image", imageUrl, additionalParams)

    private fun audioBody(channel: String, additionalParams : Map<String, Any>? = null): Map<String, Any> =
        mediaBody(channel, "audio", audioUrl, additionalParams)

    private fun videoBody(channel: String, additionalParams : Map<String, Any>? = null): Map<String, Any> =
        mediaBody(channel, "video", videoUrl, additionalParams)

    private fun fileBody(channel: String, additionalParams : Map<String, Any>? = null): Map<String, Any> =
        mediaBody(channel, "file", fileUrl, additionalParams)

    private fun whatsappCustomBody(params: Map<String, Any>): Map<String, Any> =
        baseBody("custom", whatsappChannel) + mapOf("custom" to params)

    @Test
    fun `send message exception responses`() {
        assertApiResponseException<MessageResponseException>(sendUrl, HttpMethod.POST) {
            messagesClient.send(smsText {
                from(altNumber); to(toNumber); text(text)
            })
        }
    }

    @Test
    fun `send SMS text all parameters`() {
        val clientRef = "My reference"
        val webhookUrl = "https://example.com/status"
        val ttl = 9000
        val contentId = "1107457532145798767"
        val entityId = "1101456324675322134"

        testSend(textBody("sms", mapOf(
            "client_ref" to clientRef,
            "ttl" to ttl,
            "webhook_url" to webhookUrl,
            "webhook_version" to "v0.1",
            "sms" to mapOf(
                "content_id" to contentId,
                "entity_id" to entityId
            )
        )), smsText {
            from(altNumber); to(toNumber); text(text); ttl(ttl);
            clientRef(clientRef); contentId(contentId); entityId(entityId)
            webhookUrl(webhookUrl); webhookVersion(MessagesVersion.V0_1)
        })
    }

    @Test
    fun `send SMS text required parameters`() {
        testSend(textBody("sms"), smsText {
            from(altNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send WhatsApp text`() {
        testSend(textBody(whatsappChannel), whatsappText {
            from(altNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send Viber text`() {
        testSend(textBody(viberChannel), viberText {
            from(altNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send Messenger text`() {
        testSend(textBody(messengerChannel), messengerText {
            from(altNumber); to(toNumber); text(text)
        })
    }

    @Test
    fun `send MMS vCard`() {
        val vcardUrl = "https://example.com/conatact.vcf"
        testSend(mediaBody(mmsChannel, "vcard", vcardUrl, captionMap), mmsVcard {
            from(altNumber); to(toNumber); url(vcardUrl); caption(caption)
        })
    }

    @Test
    fun `send MMS image`() {
        testSend(imageBody(mmsChannel, captionMap), mmsImage {
            from(altNumber); to(toNumber); url(imageUrl); caption(caption)
        })
    }

    @Test
    fun `send WhatsApp image`() {
        testSend(imageBody(whatsappChannel, captionMap), whatsappImage {
            from(altNumber); to(toNumber); url(imageUrl); caption(caption)
        })
    }

    @Test
    fun `send Viber image`() {
        testSend(imageBody(viberChannel), viberImage {
            from(altNumber); to(toNumber); url(imageUrl)
        })
    }

    @Test
    fun `send Messenger image`() {
        testSend(imageBody(messengerChannel), messengerImage {
            from(altNumber); to(toNumber); url(imageUrl)
        })
    }

    @Test
    fun `send MMS audio`() {
        testSend(audioBody(mmsChannel, captionMap), mmsAudio {
            from(altNumber); to(toNumber); url(audioUrl); caption(caption)
        })
    }

    @Test
    fun `send WhatsApp audio`() {
        testSend(audioBody(whatsappChannel), whatsappAudio {
            from(altNumber); to(toNumber); url(audioUrl)
        })
    }

    @Test
    fun `send Messenger audio`() {
        testSend(audioBody(messengerChannel), messengerAudio {
            from(altNumber); to(toNumber); url(audioUrl)
        })
    }

    @Test
    fun `send MMS video`() {
        testSend(videoBody(mmsChannel, captionMap), mmsVideo {
            from(altNumber); to(toNumber); url(videoUrl); caption(caption)
        })
    }

    @Test
    fun `send WhatsApp video`() {
        testSend(videoBody(whatsappChannel, captionMap), whatsappVideo {
            from(altNumber); to(toNumber); url(videoUrl); caption(caption)
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
                from(altNumber); to(toNumber); url(videoUrl); caption(caption);
                category(Category.TRANSACTION); duration(duration); ttl(ttl);
                fileSize(fileSize); thumbUrl(thumbUrl)
            }
        )
    }

    @Test
    fun `send Messenger video`() {
        testSend(videoBody(messengerChannel), messengerVideo {
            from(altNumber); to(toNumber); url(videoUrl)
        })
    }

    @Test
    fun `send WhatsApp file`() {
        val fileName = "Document.pdf"
        testSend(fileBody(whatsappChannel, captionMap + mapOf("name" to fileName)), whatsappFile {
            from(altNumber); to(toNumber); url(fileUrl); caption(caption); name(fileName)
        })
    }

    @Test
    fun `send Viber file`() {
        val fileName = "report.docx"
        testSend(fileBody(viberChannel, mapOf("name" to fileName)), viberFile {
            from(altNumber); to(toNumber); url(fileUrl); name(fileName)
        })
    }

    @Test
    fun `send Messenger file`() {
        testSend(fileBody(messengerChannel), messengerFile {
            from(altNumber); to(toNumber); url(fileUrl)
        })
    }

    @Test
    fun `send WhatsApp sticker`() {
        val stickerType = "sticker"
        val stickerUrl = "https://example.com/image.webp"
        testSend(mediaBody(whatsappChannel, stickerType, stickerUrl), whatsappSticker {
            from(altNumber); to(toNumber); url(stickerUrl)
        })

        val stickerId = "aabb7a31-1d1f-4755-a574-2971d831cd5b"
        assertEquals(UUID.fromString(stickerId), whatsappSticker {
                from(altNumber); to(toNumber); id(stickerId)
            }.sticker.id
        )

        assertThrows<IllegalStateException> { whatsappSticker {
            from(altNumber); to(toNumber); id(stickerId); url(stickerUrl)
        } }
    }

    @Test
    fun `send WhatsApp template`() {
        val messageContext = UUID.randomUUID().toString()
        val name = "9b6b4fcb_da19_4a26_8fe8_78074a91b584:verify"
        val templateParams = listOf("Un", "Deux", "Trois")

        val expectedBodyParams = baseBody("template", whatsappChannel) + mapOf(
            "webhook_version" to "v1",
            "context" to mapOf("message_uuid" to messageContext),
            "whatsapp" to mapOf(
                "policy" to "deterministic",
                "locale" to "fa"
            ),
            "template" to mapOf(
                "name" to name,
                "parameters" to templateParams
            )
        )

        val request = whatsappTemplate {
            from(altNumber); to(toNumber); webhookVersion(MessagesVersion.V1)
            policy(Policy.DETERMINISTIC); locale(Locale.PERSIAN)
            contextMessageId(messageContext); name(name); parameters(templateParams)
        }

        testSend(expectedBodyParams, request)
    }

    @Test
    fun `send WhatsApp custom`() {
        val customParams = mapOf(
            "type" to "contacts",
            "contacts" to listOf(
                mapOf(
                    "addresses" to listOf(mapOf(
                        "city" to "Birmingham"
                    ))
                )
            )
        )

        testSend(whatsappCustomBody(customParams), whatsappCustom {
            from(altNumber); to(toNumber); custom(customParams)
        })
    }

    @Test
    fun `send WhatsApp location`() {
        val latitude = 51.5356396; val longitude = -0.1077174
        val name = "Business Design Centre"
        val address = "52 Upper St, London N1 0QH"

        val params = whatsappCustomBody(mapOf(
                "type" to "location",
                "location" to mapOf(
                    "lat" to latitude, "long" to longitude,
                    "name" to name, "address" to address
                )
            )
        )

        testSend(params, whatsappLocation {
            from(altNumber); to(toNumber)
            name(name); address(address)
            latitude(latitude); longitude(longitude)
        })
    }

    @Test
    fun `send WhatsApp single product`() {
        val bodyText = "Check this out:"
        val footerText = "Hurry! While stocks last."
        val catalogId = "Cat 1"
        val productId = "prod_746"

        val params = whatsappCustomBody(mapOf(
            "type" to "interactive",
            "interactive" to mapOf(
                "type" to "product",
                "body" to mapOf("text" to bodyText),
                "footer" to mapOf("text" to footerText),
                "action" to mapOf(
                    "catalog_id" to catalogId,
                    "product_retailer_id" to productId
                )
            )
        ))

        testSend(params, whatsappSingleProduct {
            from(altNumber); to(toNumber)
            bodyText(bodyText); footerText(footerText)
            catalogId(catalogId); productRetailerId(productId)
        })
    }

    @Test
    fun `send WhatsApp multi product`() {
        val headerText = "Recommended"
        val bodyText = "Check out our cool range of products"
        val footerText = "Sale now on! Hurry"
        val catalogId = "12345"
        val title1 = "Fruits"
        val products1 = listOf("Apples", "Bananas", "Pears", "Grapes", "Satsumas")
        val title2 = "Misc."
        val product2 = UUID.randomUUID().toString()

        val params = whatsappCustomBody(mapOf(
            "type" to "interactive",
            "interactive" to mapOf(
                "type" to "product_list",
                "header" to mapOf("type" to "text", "text" to headerText),
                "body" to mapOf("text" to bodyText),
                "footer" to mapOf("text" to footerText),
                "action" to mapOf(
                    "catalog_id" to catalogId,
                    "sections" to listOf(
                        mapOf(
                            "title" to title1,
                            "product_items" to products1.map { product ->
                                mapOf("product_retailer_id" to product)
                            }
                        ),
                        mapOf(
                            "title" to title2,
                            "product_items" to listOf(mapOf("product_retailer_id" to product2))
                        )
                    )
                )
            )
        ))

        testSend(params, whatsappMultiProduct {
            from(altNumber); to(toNumber); catalogId(catalogId)
            headerText(headerText); bodyText(bodyText); footerText(footerText)
            addProductsSection(title1, products1)
            addProductsSection(title2, product2)
        })
    }

    @Test
    fun `parse inbound MMS image`() {
        val networkCode = "54123"
        val parsed = InboundMessage.fromJson(
            """
                {
                   "channel": "$mmsChannel",
                   "message_uuid": "$messageUuid",
                   "to": "$toNumber",
                   "from": "$altNumber",
                   "timestamp": "$timestamp2Str",
                   "origin": {
                      "network_code": "$networkCode"
                   },
                   "message_type": "image",
                   "image": {
                      "url": "$imageUrl",
                      "name": "image.jpg",
                      "caption": "$caption"
                   }
                }
            """
        )
        assertNotNull(parsed)
        assertEquals(Channel.MMS, parsed.channel)
        assertEquals(messageUuid, parsed.messageUuid)
        assertEquals(toNumber, parsed.to)
        assertEquals(altNumber, parsed.from)
        assertEquals(timestamp2, parsed.timestamp)
        assertEquals(networkCode, parsed.networkCode)
        assertEquals(MessageType.IMAGE, parsed.messageType)
        assertEquals(URI.create(imageUrl), parsed.imageUrl)
        assertEquals(caption, parsed.imageCaption)
    }
}