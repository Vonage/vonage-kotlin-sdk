package com.vonage.client.kt

import com.vonage.client.common.HttpMethod
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
    private val dtmf = "p*123#"
    private val streamUrl = "https://example.com/waiting.mp3"
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
        "direction" to "inbound",
        "rate" to rate,
        "price" to price,
        "duration" to duration,
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
        assertEquals(CallDirection.INBOUND, callInfo.direction)
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

    private fun assertExistingCall404(url: String, requestMethod: HttpMethod, invocation: () -> Any) {
        assertApiResponseException<VoiceResponseException>(url, requestMethod, invocation, 404,
            title = "Not Found", detail = "Call $callIdStr doesn't exist."
        )
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
        assertExistingCall404(callUrl, HttpMethod.PUT, invocation)
    }

    private fun testStream(loop: Int = 1, level: Double = 0.0, invocation: (() -> StreamResponse)? = null) {

        val message = "Stream ${if (invocation == null) "stopped" else "started"}"
        val expectedResponseParams = mapOf("message" to message, "uuid" to callIdStr)
        val streamUrl = "$callUrl/stream"
        val response = if (invocation == null) {
            mockDelete(streamUrl, expectedResponseParams = expectedResponseParams)
            callObj.stopStream()
        }
        else {
            mockPut(streamUrl, status = 200,
                expectedRequestParams = mapOf(
                    "stream_url" to listOf(this.streamUrl),
                    "loop" to loop, "level" to level
                ),
                expectedResponseParams = expectedResponseParams
            )
            invocation.invoke()
        }
        assertNotNull(response)
        assertEquals(message, response.message)
        assertEquals(callIdStr, response.uuid)

        if (invocation != null) assertExistingCall404(streamUrl, HttpMethod.PUT, invocation)
        else assertExistingCall404(streamUrl, HttpMethod.DELETE, callObj::stopStream)
    }

    private fun testTextToSpeech(expectedRequestParams: Map<String, Any>? = null, invocation: () -> TalkResponse) {
        val message = if (expectedRequestParams != null) "started" else "stopped"
        val expectedResponseParams = mapOf("message" to message, "uuid" to callIdStr)
        val talkUrl = "$callUrl/talk"
        if (expectedRequestParams != null) {
            mockPut(talkUrl, expectedRequestParams, expectedResponseParams = expectedResponseParams)
        }
        else {
            mockDelete(talkUrl, AuthType.JWT, expectedResponseParams)
        }
        val response = invocation.invoke()
        assertNotNull(response)
        assertEquals(message, response.message)
        assertEquals(callIdStr, response.uuid)

        assertExistingCall404(talkUrl,
            if (expectedRequestParams != null) HttpMethod.PUT else HttpMethod.DELETE,
            invocation
        )
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
        assertEqualsSampleCall(callObj.info())
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
    fun `send dtmf`() {
        val message = "DTMF sent"
        mockPut(expectedUrl = "$callUrl/dtmf", status = 200,
            expectedRequestParams = mapOf("digits" to dtmf),
            expectedResponseParams = mapOf("message" to message, "uuid" to callIdStr)
        )
        val response = callObj.sendDtmf(dtmf)
        assertNotNull(response)
        assertEquals(message, response.message)
        assertEquals(callIdStr, response.uuid)
    }

    @Test
    fun `stream audio into call`() {
        val loop = 3
        val level = 0.45
        testStream {
            callObj.streamAudio(streamUrl)
        }
        testStream(loop = loop) {
            callObj.streamAudio(streamUrl, loop)
        }
        testStream(level = level) {
            callObj.streamAudio(streamUrl, level = level)
        }
        testStream(loop = loop, level = level) {
            callObj.streamAudio(streamUrl, loop = loop, level = level)
        }
    }

    @Test
    fun `stop audio stream`() {
        testStream()
    }

    @Test
    fun `play text to speech all parameters`() {
        val style = 1
        val premium = true
        val loop = 3
        val level = 0.65
        testTextToSpeech(mapOf(
            "text" to text, "language" to "en-GB-SCT",
            "style" to style, "premium" to premium,
            "loop" to loop, "level" to level
        )) {
            callObj.startTalk(text) {
                language(TextToSpeechLanguage.SCOTTISH_ENGLISH)
                style(style); premium(premium); loop(loop); level(level)
            }
        }
    }

    @Test
    fun `play text to speech text only`() {
        testTextToSpeech(mapOf("text" to text)) {
            callObj.startTalk(text)
        }
    }

    @Test
    fun `stop text to speech`() {
        testTextToSpeech {
            callObj.stopTalk()
        }
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

    @Test
    fun `create call to all endpoint types with all fields and answer url`() {
        val answerUrl = "https://example.com/answer"
        val eventUrl = "https://example.com/event"
        val websocketUri = "wss://example.com/socket"
        val sipUri = "sip:rebekka@sip.example.com"
        val customHeaders = mapOf(
            "customer_id" to "abc123",
            "purchases" to 19,
            "Cat person" to true,
            "Cars" to listOf("M240i", "M2 CS", "C63s", "RS 3"),
            "Location" to "NY"
        )
        val fromPstn = "14155550100"
        val lengthTimer = 5600
        val ringingTimer = 42
        val beepTimeout = 78
        val answerMethod = HttpMethod.GET
        val eventMethod = HttpMethod.POST
        val amdBehaviour = MachineDetection.HANGUP
        val amdMode = AdvancedMachineDetection.Mode.DETECT_BEEP
        val callStatus = CallStatus.RINGING
        val callDirection = CallDirection.OUTBOUND
        val wsContentType = "audio/l16;rate=8000"
        val vbcExt = "4321"
        val userToUserHeader = "56a390f3d2b7310023a"

        mockPost(callsBaseUrl, expectedRequestParams = mapOf(
            "answer_url" to listOf(answerUrl),
            "answer_method" to answerMethod.name,
            "to" to listOf(
                mapOf(
                    "type" to phoneType,
                    "number" to toNumber,
                    "dtmfAnswer" to dtmf
                ),
                mapOf(
                    "type" to "vbc",
                    "extension" to vbcExt
                ),
                mapOf(
                    "type" to "websocket",
                    "uri" to websocketUri,
                    "content-type" to wsContentType,
                    "headers" to customHeaders
                ),
                mapOf(
                    "type" to "sip",
                    "uri" to sipUri,
                    "headers" to customHeaders,
                    "standard_headers" to mapOf("User-to-User" to userToUserHeader)
                )
            ),
            "from" to mapOf(
                "type" to phoneType,
                "number" to fromPstn
            ),
            "random_from_number" to false,
            "event_url" to listOf(eventUrl),
            "event_method" to eventMethod,
            "advanced_machine_detection" to mapOf(
                "behavior" to amdBehaviour.name.lowercase(),
                "mode" to amdMode.name.lowercase(),
                "beep_timeout" to beepTimeout
            ),
            "length_timer" to lengthTimer,
            "ringing_timer" to ringingTimer
        ), status = 201, expectedResponseParams = mapOf(
            "uuid" to callIdStr,
            "status" to callStatus.name.lowercase(),
            "direction" to callDirection.name.lowercase(),
            "conversation_uuid" to conversationId
        ))

        val callEvent = voiceClient.createCall {
            answerUrl(answerUrl); answerMethod(answerMethod)
            from(fromPstn); fromRandomNumber(false);
            eventUrl(eventUrl); eventMethod(eventMethod)
            lengthTimer(lengthTimer); ringingTimer(ringingTimer)
            advancedMachineDetection {
                behavior(amdBehaviour); beepTimeout(beepTimeout); mode(amdMode)
            }
            to(
                PhoneEndpoint(toNumber, dtmf), VbcEndpoint(vbcExt),
                WebSocketEndpoint(websocketUri, wsContentType, customHeaders),
                SipEndpoint(sipUri, customHeaders, userToUserHeader)
            )
        }

        assertNotNull(callEvent)
        assertEquals(callIdStr, callEvent.uuid)
        assertEquals(callStatus, callEvent.status)
        assertEquals(callDirection, callEvent.direction)
        assertEquals(conversationId, callEvent.conversationUuid)
    }
}