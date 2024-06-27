package com.vonage.client.kt

import com.vonage.client.voice.CallDirection
import com.vonage.client.voice.CallStatus
import com.vonage.client.voice.PhoneEndpoint
import com.vonage.client.voice.ncco.Ncco
import com.vonage.client.voice.ncco.TalkAction
import java.net.URI
import java.time.Instant
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VoiceTest : AbstractTest() {
    private val voiceClient = vonage.voice
    private val callsBaseUrl = "/v1/calls"
    private val callIdStr = "63f61863-4a51-4f6b-86e1-46edebcf9356"
    private val callUrl = "$callsBaseUrl/$callIdStr"
    private val callObj = voiceClient.call(UUID.fromString(callIdStr))
    private val conversationId = "CON-f972836a-550f-45fa-956c-12a2ab5b7d22"

    private fun testModifyCall(actionName: String = "transfer", invocation: () -> Unit,
                               nccoAction: Map<String, Any>? = null, nccoUrl: String? = null) {
        mockPut(expectedUrl = callUrl,
            expectedRequestParams = mapOf("action" to actionName) + (
                        if (actionName == "transfer") mapOf(
                            "destination" to mapOf("type" to "ncco") +
                            if (nccoUrl != null) mapOf("url" to listOf(nccoUrl))
                            else mapOf("ncco" to listOf(nccoAction))
                        ) else mapOf()
                    ),
            status = 204
        )
        invocation.invoke()
    }

    @Test
    fun `terminate call`() {
        testModifyCall("hangup", callObj::hangup)
    }

    @Test
    fun `mute call`() {
        testModifyCall("mute", callObj::mute)
    }

    @Test
    fun `umute call`() {
        testModifyCall("unmute", callObj::unmute)
    }

    @Test
    fun `earmuff call`() {
        testModifyCall("earmuff", callObj::earmuff)
    }

    @Test
    fun `umearmuff call`() {
        testModifyCall("unearmuff", callObj::unearmuff)
    }

    @Test
    fun `get call`() {
        val price = "23.40"
        val duration = 60
        val rate = "0.39"
        val phoneType = "phone"

        mockGet(expectedUrl = callUrl, expectedResponseParams = mapOf(
            "_links" to mapOf(
                "self" to mapOf(
                    "href" to "/calls/$callIdStr"
                )
            ),
            "uuid" to callIdStr,
            "conversation_uuid" to conversationId,
            "to" to mapOf(
                "type" to phoneType,
                "number" to toNumber
            ),
            "from" to mapOf(
                "type" to phoneType,
                "number" to altNumber
            ),
            "status" to "completed",
            "direction" to "outbound",
            "rate" to rate,
            "price" to price,
            "duration" to "$duration",
            "start_time" to startTime,
            "end_time" to endTime,
            "network" to networkCode
        ))

        val response = callObj.get()
        assertNotNull(response)
        assertEquals(callIdStr, response.uuid)
        assertEquals(conversationId, response.conversationUuid)
        val to = response.to
        assertNotNull(to)
        assertEquals(phoneType, to.type)
        assertEquals(toNumber, (to as PhoneEndpoint).number)
        val from = response.from
        assertNotNull(from)
        assertEquals(phoneType, from.type)
        assertEquals(altNumber, (from as PhoneEndpoint).number)
        assertEquals(CallStatus.COMPLETED, response.status)
        assertEquals(CallDirection.OUTBOUND, response.direction)
        assertEquals(rate, response.rate)
        assertEquals(price, response.price)
        assertEquals(duration, response.duration)
        assertEquals(Instant.parse(startTime), response.startTime.toInstant())
        assertEquals(Instant.parse(endTime), response.endTime.toInstant())
        assertEquals(networkCode, response.network)
    }

    @Test
    fun `transfer call with answer url`() {
        val answerUrl = "https://example.com/ncco.json"
        testModifyCall(nccoUrl = answerUrl, invocation = {
            callObj.transfer(URI.create(answerUrl))
            callObj.transfer(answerUrl)
        })
    }

    @Test
    fun `transfer call with ncco`() {
        testModifyCall(nccoAction = mapOf("action" to "talk", "text" to text), invocation = {
            callObj.transfer(Ncco(TalkAction.builder(text).build()))
        })
    }
}