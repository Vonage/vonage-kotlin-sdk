package com.vonage.client.kt

import com.vonage.client.voice.*
import com.vonage.client.voice.ncco.Ncco
import com.vonage.client.voice.ncco.TalkAction
import java.net.URI
import java.time.Instant
import java.util.*
import kotlin.test.Test
import kotlin.test.*

class VoiceTest : AbstractTest() {
    private val voiceClient = vonage.voice
    private val callsBaseUrl = "/v1/calls"
    private val callIdStr = "63f61863-4a51-4f6b-86e1-46edebcf9356"
    private val callUrl = "$callsBaseUrl/$callIdStr"
    private val callObj = voiceClient.call(UUID.fromString(callIdStr))
    private val conversationId = "CON-f972836a-550f-45fa-956c-12a2ab5b7d22"
    private val price = "23.40"
    private val duration = 60
    private val rate = "0.39"
    private val phoneType = "phone"
    private val count = 89
    private val pageSize = 25
    private val recordIndex = 14
    private val callResponseMap = mapOf(
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
    )
    private val listCallsResponse = mapOf(
        "count" to count,
        "page_size" to pageSize,
        "record_index" to recordIndex,
        "_links" to mapOf("self" to mapOf(
            "href" to "/calls?page_size=$pageSize&record_index=$recordIndex&order=desc"
        )),
        "_embedded" to mapOf("calls" to listOf(
            mapOf(),
            callResponseMap
        ))
    )

    private fun assertEqualsSampleCall(callInfo: CallInfo) {
        assertNotNull(callInfo)
        assertEquals(callIdStr, callInfo.uuid)
        assertEquals(conversationId, callInfo.conversationUuid)
        val to = callInfo.to
        assertNotNull(to)
        assertEquals(phoneType, to.type)
        assertEquals(toNumber, (to as PhoneEndpoint).number)
        val from = callInfo.from
        assertNotNull(from)
        assertEquals(phoneType, from.type)
        assertEquals(altNumber, (from as PhoneEndpoint).number)
        assertEquals(CallStatus.COMPLETED, callInfo.status)
        assertEquals(CallDirection.OUTBOUND, callInfo.direction)
        assertEquals(rate, callInfo.rate)
        assertEquals(price, callInfo.price)
        assertEquals(duration, callInfo.duration)
        assertEquals(Instant.parse(startTime), callInfo.startTime.toInstant())
        assertEquals(Instant.parse(endTime), callInfo.endTime.toInstant())
        assertEquals(networkCode, callInfo.network)
    }

    private fun assertEqualsSampleCallsPage(callsPage: CallInfoPage) {
        assertNotNull(callsPage)
        assertEquals(pageSize, callsPage.pageSize)
        assertEquals(recordIndex, callsPage.recordIndex)
        assertNotNull(callsPage.links?.self?.href)
        assertEquals(count, callsPage.count)
        val infos = callsPage.embedded?.callInfos
        assertNotNull(infos)
        assertEquals(2, infos.size)
        assertNotNull(infos[0])
        assertEqualsSampleCall(infos[1])
    }

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
        mockGet(expectedUrl = callUrl, expectedResponseParams = callResponseMap)
        assertEqualsSampleCall(callObj.get())
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

    @Test
    fun `list calls all filter parameters`() {
        mockGet(callsBaseUrl, expectedQueryParams = mapOf(
            "status" to "unanswered",
            "date_start" to startTime,
            "date_end" to endTime,
            "page_size" to pageSize,
            "record_index" to recordIndex,
            "order" to "desc",
            "conversation_uuid" to conversationId

        ), expectedResponseParams = listCallsResponse)

        val callsPage = voiceClient.listCalls {
            status(CallStatus.UNANSWERED)
            dateStart(startTime); dateEnd(endTime)
            pageSize(pageSize); recordIndex(recordIndex)
            order(CallOrder.DESCENDING); conversationUuid(conversationId)
        }

        assertEqualsSampleCallsPage(callsPage)
    }

    @Test
    fun `list calls no filter`() {
        mockGet(callsBaseUrl, expectedResponseParams = listCallsResponse)
        assertEqualsSampleCallsPage(voiceClient.listCalls())
    }

}