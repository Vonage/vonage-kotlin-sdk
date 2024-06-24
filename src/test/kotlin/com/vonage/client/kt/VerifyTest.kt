package com.vonage.client.kt

import org.junit.jupiter.api.Test
import java.net.URI
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VerifyTest : AbstractTest() {
    private val verifyClient = vonageClient.verify
    private val baseUrl = "/v2/verify"
    private val requestId = "c11236f4-00bf-4b89-84ba-88b25df97315"

    @Test
    fun `send verification all workflows and parameters`() {
        val brand = "Nexmo KT"
        val clientRef = "my-personal-reference"
        val timeout = 60
        val fraudCheck = false
        val sandbox = true
        val codeLength = 5
        val locale = "ja-jp"
        val whatsappNumber = "447700400080"
        val entityId = "1101407360000017170"
        val contentId = "1107158078772563946"
        val appHash = "ABC123def45"
        val toEmail = "alice@example.com"
        val fromEmail = "bob@example.org"
        val checkUrl = "https://api.nexmo.com/v2/verify/c11236f4-00bf-4b89-84ba-88b25df97315/silent-auth/redirect"
        val redirectUrl = "https://acme-app.com/sa/redirect"

        mockJsonJwtPost(baseUrl,
            expectedRequestParams = mapOf(
                "brand" to brand,
                "client_ref" to clientRef,
                "channel_timeout" to timeout,
                "code_length" to codeLength,
                "locale" to "ja-jp",
                "fraud_check" to fraudCheck,
                "workflow" to listOf(
                    mapOf(
                        "channel" to "silent_auth",
                        "to" to toNumber,
                        "sandbox" to sandbox,
                        "redirect_url" to redirectUrl
                    ),
                    mapOf(
                        "channel" to "voice",
                        "to" to altNumber
                    ),
                    mapOf(
                        "channel" to "sms",
                        "to" to toNumber,
                        "from" to altNumber,
                        "content_id" to contentId,
                        "entity_id" to entityId,
                        "app_hash" to appHash
                    ),
                    mapOf(
                        "channel" to "email",
                        "to" to toEmail,
                        "from" to fromEmail
                    ),
                    mapOf(
                        "channel" to "whatsapp",
                        "to" to altNumber,
                        "from" to whatsappNumber
                    ),
                    mapOf(
                        "channel" to "whatsapp_interactive",
                        "to" to toNumber,
                        "from" to whatsappNumber
                    )
                )
            ),
            expectedResponseParams = mapOf(
                "request_id" to requestId,
                "check_url" to checkUrl
            )
        )

        val response = verifyClient.sendVerification {
            brand(brand); clientRef(clientRef); channelTimeout(timeout)
            fraudCheck(fraudCheck); codeLength(codeLength); locale(locale)
            silentAuth(toNumber, sandbox, redirectUrl); voice(altNumber); sms(toNumber) {
                entityId(entityId); contentId(contentId); appHash(appHash); from(altNumber)
            }; email(toEmail, fromEmail)
            whatsapp(altNumber, whatsappNumber); whatsappCodeless(toNumber, whatsappNumber)
        }

        assertNotNull(response)
        assertEquals(UUID.fromString(requestId), response.requestId)
        assertEquals(URI.create(checkUrl), response.checkUrl)
    }

    @Test
    fun `cancel verification`() {
        mockDelete("$baseUrl/$requestId")
        verifyClient.cancelVerification(requestId)
    }
}