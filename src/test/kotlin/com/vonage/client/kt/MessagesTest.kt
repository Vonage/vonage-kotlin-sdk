/*
 *   Copyright 2025 Vonage
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
import com.vonage.client.common.HttpMethod
import com.vonage.client.common.MessageType
import com.vonage.client.messages.*
import com.vonage.client.messages.viber.Category
import com.vonage.client.messages.whatsapp.Locale
import com.vonage.client.messages.whatsapp.Policy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MessagesTest : AbstractTest() {
    private val client = vonage.messages
    private val authType = AuthType.JWT
    private val sendUrl = "/v1/messages"
    private val messageUuid = testUuid
    private val messageUuidStr = testUuidStr
    private val mmsChannel = "mms"
    private val rcsChannel = "rcs"
    private val whatsappChannel = "whatsapp"
    private val viberChannel = "viber_service"
    private val messengerChannel = "messenger"
    private val caption = "Additional text to accompany the media"
    private val captionMap = mapOf("caption" to caption)

    private fun testSend(expectedBodyParams: Map<String, Any>, req: MessageRequest) {
        mockPost(expectedUrl = sendUrl, status = 202, authType = authType,
            expectedRequestParams = expectedBodyParams,
            expectedResponseParams = mapOf("message_uuid" to messageUuidStr)
        )
        assertEquals(messageUuid, client.send(req))
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
            client.send(smsText {
                from(altNumber); to(toNumber); text(text)
            })
        }
    }

    @Test
    fun `send SMS text all parameters`() {
        val webhookUrl = "https://example.com/status"
        val ttl = 9000

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
            from(altNumber); to(toNumber); text(text); ttl(ttl)
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
    fun `send MMS text with TTL`() {
        val ttl = 600
        testSend(textBody(mmsChannel) + mapOf("ttl" to ttl), mmsText {
            from(altNumber); to(toNumber); text(text); ttl(ttl)
        })
    }

    @Test
    fun `send RCS text`() {
        testSend(textBody(rcsChannel), rcsText {
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
    fun `send RCS image`() {
        testSend(imageBody(rcsChannel), rcsImage {
            from(altNumber); to(toNumber); url(imageUrl)
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
    fun `send RCS video`() {
        testSend(videoBody(rcsChannel), rcsVideo {
            from(altNumber); to(toNumber); url(videoUrl)
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
                    "ttl" to ttl,
                    "file_size" to fileSize
            )), viberVideo {
                from(altNumber); to(toNumber); url(videoUrl); caption(caption)
                category(Category.TRANSACTION); duration(duration); ttl(ttl)
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
    fun `send MMS file`() {
        testSend(fileBody(mmsChannel, captionMap), mmsFile {
            from(altNumber); to(toNumber); url(fileUrl); caption(caption)
        })
    }

    @Test
    fun `send RCS file`() {
        testSend(fileBody(rcsChannel), rcsFile {
            from(altNumber); to(toNumber); url(fileUrl)
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
    fun `send MMS content`() {
        testSend(baseBody("content", mmsChannel) + mapOf(
            "content" to listOf(
                mapOf("type" to "file", "url" to fileUrl, "caption" to caption),
                mapOf("type" to "image", "url" to imageUrl, "caption" to caption),
                mapOf("type" to "audio", "url" to audioUrl, "caption" to caption),
                mapOf("type" to "video", "url" to videoUrl, "caption" to caption),
                mapOf("type" to "vcard", "url" to vcardUrl, "caption" to caption)
            )
        ),
            mmsContent {
                from(altNumber); to(toNumber)
                addFile(fileUrl, caption)
                addImage(imageUrl, caption)
                addAudio(audioUrl, caption)
                addVideo(videoUrl, caption)
                addVcard(vcardUrl, caption)
            }
        )
    }

    @Test
    fun `send WhatsApp reaction`() {
        val emoji = "😍"
        testSend(
            baseBody("reaction", whatsappChannel) + mapOf(
                "reaction" to mapOf(
                    "action" to "react",
                    "emoji" to emoji
                ),
                "context" to mapOf(
                    "message_uuid" to messageUuidStr
                )
            ),
            whatsappReaction {
                from(altNumber); to(toNumber); reaction(emoji)
                contextMessageId(messageUuidStr)
            }
        )
    }

    @Test
    fun `send WhatsApp unreaction`() {
        testSend(
            baseBody("reaction", whatsappChannel) + mapOf(
                "reaction" to mapOf(
                    "action" to "unreact"
                ),
                "context" to mapOf(
                    "message_uuid" to messageUuidStr
                )
            ),
            whatsappReaction {
                from(altNumber); to(toNumber); unreact()
                contextMessageId(messageUuidStr)
            }
        )
    }

    @Test
    fun `send WhatsApp sticker`() {
        val stickerUrl = "https://example.com/image.webp"
        testSend(mediaBody(whatsappChannel, "sticker", stickerUrl), whatsappSticker {
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
                    "latitude" to latitude, "longitude" to longitude,
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
    fun `send RCS suggested actions`() {
        val postback = "postback_data_1234"
        val payload = mapOf(
            "contentMessage" to mapOf(
                "text" to "Need some help? Call us now or visit our website for more information.",
                "suggestions" to listOf(
                    mapOf(
                        "action" to mapOf(
                            "text" to "Call us",
                            "postbackData" to postback,
                            "fallbackUrl" to "$exampleUrlBase/contact/",
                            "dialAction" to mapOf(
                                "phoneNumber" to "+447900000000"
                            )
                        )
                    ),
                    mapOf(
                        "action" to mapOf(
                            "text" to "Visit site",
                            "postbackData" to postback,
                            "openUrlAction" to mapOf(
                                "url" to exampleUrlBase
                            )
                        )
                    ),
                    mapOf(
                        "action" to mapOf(
                            "text" to "Save to calendar",
                            "postbackData" to postback,
                            "fallbackUrl" to "https://www.google.com/calendar",
                            "createCalendarEventAction" to mapOf(
                                "startTime" to "2024-06-28T19:00:00Z",
                                "endTime" to "2024-06-28T20:00:00Z",
                                "title" to "Vonage API Product Launch",
                                "description" to "Event to demo Vonage's new and exciting API product"
                            )
                        )
                    ),
                    mapOf(
                        "action" to mapOf(
                            "text" to "View map",
                            "postbackData" to postback,
                            "fallbackUrl" to "https://www.google.com/maps/place/Vonage/@51.5230371,-0.0852492,15z",
                            "viewLocationAction" to mapOf(
                                "latLong" to mapOf(
                                    "latitude" to "51.5230371",
                                    "longitude" to "-0.0852492"
                                ),
                                "label" to "Vonage London Office"
                            )
                        )
                    ),
                    mapOf(
                        "action" to mapOf(
                            "text" to "Share a location",
                            "postbackData" to postback,
                            "shareLocationAction" to emptyMap<String, Any>()
                        )
                    )
                )
            )
        )
        testSend(
            baseBody("custom", rcsChannel) + mapOf("custom" to payload),
            rcsCustom {
                from(altNumber); to(toNumber); custom(payload)
            }
        )
    }

    @Test
    fun `revoke outbound message`() {
        mockPatch(
            expectedUrl = "$sendUrl/$messageUuidStr",
            expectedRequestParams = mapOf("status" to "revoked"),
            authType = authType, status = 200
        )
        client.existingMessage(messageUuidStr, ApiRegion.API_US).revoke()
    }

    @Test
    fun `mark inbound message as read`() {
        mockPatch(
            expectedUrl = "/v1/messages/$messageUuidStr",
            expectedRequestParams = mapOf("status" to "read"),
            authType = AuthType.JWT, status = 200
        )
        client.existingMessage(messageUuidStr, ApiRegion.API_AP).markAsRead()
    }

    @Test
    fun `parse inbound MMS image`() {
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
                   },
                   "content": [
                      {
                         "type": "image",
                         "url": "$imageUrl",
                         "caption": "$caption"
                      }
                   ]
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
        val content = parsed.content
        assertNotNull(content)
        assertEquals(1, content.size)
        val contentImage = content[0]
        assertEquals(MessageType.IMAGE, contentImage.type)
        assertEquals(URI.create(imageUrl), contentImage.url)
        assertEquals(caption, contentImage.caption)
    }

    @Test
    fun `parse status update`() {
        val statusEnum = MessageStatus.Status.SUBMITTED
        val currency = "EUR"
        val amount = 0.0333
        val channel = Channel.SMS
        val smsCount = 2
        val parsed = MessageStatus.fromJson(
            """
                {
                   "message_uuid": "$messageUuidStr",
                   "to": "$toNumber",
                   "from": "$altNumber",
                   "timestamp": "$timestampStr",
                   "status": "${statusEnum.name.lowercase()}",
                   "error": {
                      "error": {
                         "type": "https://developer.vonage.com/api-errors/messages#1000",
                         "title": 1000,
                         "detail": "Throttled - You have exceeded the submission capacity allowed on this account. Please wait and retry",
                         "instance": "bf0ca0bf927b3b52e3cb03217e1a1ddf"
                      }
                   },
                   "client_ref": "$clientRef",
                   "usage": {
                      "currency": "$currency",
                      "price": "$amount"
                   },
                   "channel": "${channel.name.lowercase()}",
                   "destination": {
                      "network_code": "$networkCode"
                   },
                   "sms": {
                      "count_total": "$smsCount"
                   }
                }
            """
        )
        assertNotNull(parsed)
        assertEquals(messageUuid, parsed.messageUuid)
        assertEquals(toNumber, parsed.to)
        assertEquals(altNumber, parsed.from)
        assertEquals(timestamp, parsed.timestamp)
        assertEquals(statusEnum, parsed.status)
        assertEquals(clientRef, parsed.clientRef)
        val usage = parsed.usage
        assertNotNull(usage)
        assertEquals(Currency.getInstance(currency), usage.currency)
        assertEquals(amount, usage.price)
        assertEquals(channel, parsed.channel)
        assertEquals(networkCode, parsed.destinationNetworkCode)
        assertEquals(smsCount, parsed.smsTotalCount)
    }

    @Test
    fun `send sandbox real request fails with 401`() {
        try {
            client.send(
                smsText { from(altNumber); to(toNumber); text(text) },
                sandbox = true
            )
        }
        catch (ex: MessageResponseException) {
            assertEquals(401, ex.statusCode)
            assertNotNull(ex.title)
            assertNotNull(ex.detail)
            assertNotNull(ex.instance)
        }
    }
}