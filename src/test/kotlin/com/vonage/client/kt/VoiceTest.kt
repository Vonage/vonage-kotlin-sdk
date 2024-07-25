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
import com.vonage.client.voice.*
import com.vonage.client.voice.ncco.*
import java.net.URI
import java.util.*
import kotlin.test.Test
import kotlin.test.*

class VoiceTest : AbstractTest() {
    private val voiceClient = vonage.voice
    private val callsBaseUrl = "/v1/calls"
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
    private val fromPstn = "14155550100"
    private val user = "Sam"
    private val vbcExt = "4321"
    private val eventUrl = "https://example.com/event"
    private val streamUrl = "https://example.com/waiting.mp3"
    private val onAnswerUrl = "https://example.com/ncco.json"
    private val websocketUri = "wss://example.com/socket"
    private val ringbackTone = "http://example.com/ringbackTone.wav"
    private val sipUri = "sip:rebekka@sip.example.com"
    private val wsContentType = "audio/l16;rate=8000"
    private val userToUserHeader = "56a390f3d2b7310023a"
    private val conversationName = "selective-audio Demo"
    private val customHeaders = mapOf(
        "customer_id" to "abc123",
        "purchases" to 19,
        "Cat person" to true,
        "Cars" to listOf("M240i", "M2 CS", "C63s", "RS 3"),
        "Location" to "NY"
    )
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
        "start_time" to startTimeStr,
        "end_time" to endTimeStr,
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
        assertEquals(toNumber, (to as com.vonage.client.voice.PhoneEndpoint).number)
        val from = callInfo.from
        assertNotNull(from)
        assertEquals(phoneType, from.type)
        assertEquals(altNumber, (from as com.vonage.client.voice.PhoneEndpoint).number)
        assertEquals(CallStatus.COMPLETED, callInfo.status)
        assertEquals(CallDirection.INBOUND, callInfo.direction)
        assertEquals(rate, callInfo.rate)
        assertEquals(price, callInfo.price)
        assertEquals(duration, callInfo.duration)
        assertEquals(startTime, callInfo.startTime.toInstant())
        assertEquals(endTime, callInfo.endTime.toInstant())
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

    private fun testCreateCall(expectedRequestParams: Map<String, Any>, call: Call.Builder.() -> Unit) {
        val callStatus = CallStatus.RINGING
        val callDirection = CallDirection.OUTBOUND

        mockPost(callsBaseUrl, expectedRequestParams = expectedRequestParams,
            status = 201, expectedResponseParams = mapOf(
                "uuid" to callIdStr,
                "status" to callStatus.name.lowercase(),
                "direction" to callDirection.name.lowercase(),
                "conversation_uuid" to conversationId
            )
        )

        val callEvent = voiceClient.createCall(call)
        assertNotNull(callEvent)
        assertEquals(callIdStr, callEvent.uuid)
        assertEquals(callStatus, callEvent.status)
        assertEquals(callDirection, callEvent.direction)
        assertEquals(conversationId, callEvent.conversationUuid)
    }

    private fun testCreateCallToSingleEndpoint(toParams: Map<String, Any>,
                                               toBuilder: Call.Builder.() -> Call.Builder) {
        testCreateCall(mapOf(
            "random_from_number" to true,
            "answer_url" to listOf(onAnswerUrl),
            "answer_method" to "GET",
            "to" to listOf(toParams)
        )) {
            fromRandomNumber(true); answerUrl(onAnswerUrl)
            toBuilder()
        }
    }

    private fun testSingleNcco(additionalParams: Map<String, Any> = mapOf(), ncco: Action) {
        val requestParams = mapOf(
            "random_from_number" to true,
            "to" to listOf(mapOf(
                "type" to "websocket",
                "uri" to websocketUri
            )),
            "ncco" to listOf(
                mapOf("action" to ncco.action) + additionalParams
            )
        )

        testCreateCall(requestParams) {
            ncco(ncco); fromRandomNumber(true);
            to(com.vonage.client.voice.WebSocketEndpoint(websocketUri, null, null))
        }
    }

    private fun testSingleNccoConnect(params: Map<String, Any>, connectAction: ConnectAction) =
        testSingleNcco(mapOf("endpoint" to listOf(mapOf(
            "type" to connectAction.endpoint.first().type) + params)), connectAction
        )

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
        testModifyCall(nccoUrl = onAnswerUrl, invocation = {
            callObj.transfer(URI.create(onAnswerUrl))
            callObj.transfer(onAnswerUrl)
        })
    }

    @Test
    fun `transfer call with ncco`() {
        testModifyCall(nccoAction = mapOf("action" to "talk", "text" to text), invocation = {
            callObj.transfer(TalkAction.builder(text).build())
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
            dateStart(startTimeStr); dateEnd(endTimeStr)
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
    fun `create TTS call with required parameters only`() {
        val ssmlText = "<speak><prosody rate='fast'>I can speak fast.</prosody></speak>"
        testCreateCall(mapOf(
            "random_from_number" to true,
            "to" to listOf(mapOf(
                "type" to phoneType,
                "number" to toNumber
            )),
            "ncco" to listOf(mapOf(
                "action" to "talk",
                "text" to ssmlText
            ))
        )) {
            fromRandomNumber(true); ncco(talkAction(ssmlText))
            to(com.vonage.client.voice.PhoneEndpoint(toNumber))
        }
    }

    @Test
    fun `create call to PSTN`() {
        val baseMap = mapOf("type" to phoneType, "number" to toNumber)
        testCreateCallToSingleEndpoint(baseMap) {
            toPstn(toNumber)
        }

        testCreateCallToSingleEndpoint(baseMap + mapOf("dtmfAnswer" to dtmf)) {
            toPstn(toNumber, dtmf)
        }
    }

    @Test
    fun `create call to App`() {
        testCreateCallToSingleEndpoint(mapOf("type" to "app", "user" to user)) {
            toApp(user)
        }
    }

    @Test
    fun `create call to VBC`() {
        testCreateCallToSingleEndpoint(mapOf("type" to "vbc", "extension" to vbcExt)) {
            toVbc(vbcExt)
        }
    }

    @Test
    fun `create call to SIP`() {
        val baseMap = mapOf("type" to "sip", "uri" to sipUri)
        testCreateCallToSingleEndpoint(baseMap) {
            toSip(sipUri)
        }

        testCreateCallToSingleEndpoint(baseMap + mapOf(
            "headers" to customHeaders, "standard_headers" to mapOf("User-to-User" to userToUserHeader)
        )) {
            toSip(sipUri, customHeaders, userToUserHeader)
        }
    }

    @Test
    fun `create call to WebSocket`() {
        val baseMap = mapOf("type" to "websocket", "uri" to websocketUri)
        testCreateCallToSingleEndpoint(baseMap) {
            toWebSocket(websocketUri)
        }

        testCreateCallToSingleEndpoint(baseMap + mapOf(
            "content-type" to wsContentType, "headers" to customHeaders
        )) {
            toWebSocket(websocketUri, wsContentType, customHeaders)
        }
    }

    @Test
    fun `create call to all endpoint types with all fields and answer url`() {
        val answerUrl = "https://example.com/answer"
        val lengthTimer = 5600
        val ringingTimer = 42
        val beepTimeout = 78
        val answerMethod = HttpMethod.GET
        val eventMethod = HttpMethod.POST
        val amdBehaviour = MachineDetection.HANGUP
        val amdMode = AdvancedMachineDetection.Mode.DETECT_BEEP

        testCreateCall(mapOf(
            "answer_url" to listOf(answerUrl),
            "answer_method" to answerMethod.name,
            "to" to listOf(
                mapOf(
                    "type" to "app",
                    "user" to user
                ),
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
        )) {
            answerUrl(answerUrl); answerMethod(answerMethod)
            from(fromPstn); fromRandomNumber(false);
            eventUrl(eventUrl); eventMethod(eventMethod)
            lengthTimer(lengthTimer); ringingTimer(ringingTimer)
            advancedMachineDetection {
                behavior(amdBehaviour); beepTimeout(beepTimeout); mode(amdMode)
            }
            to(
                com.vonage.client.voice.AppEndpoint(user),
                com.vonage.client.voice.PhoneEndpoint(toNumber, dtmf),
                com.vonage.client.voice.VbcEndpoint(vbcExt),
                com.vonage.client.voice.WebSocketEndpoint(websocketUri, wsContentType, customHeaders),
                com.vonage.client.voice.SipEndpoint(sipUri, customHeaders, userToUserHeader)
            )
        }
    }

    @Test
    fun `create call with talk action required parameters only`() {
        testSingleNcco(mapOf("text" to text), talkAction(text))
    }

    @Test
    fun `create call with stream action required parameters only`() {
        testSingleNcco(mapOf("streamUrl" to listOf(streamUrl)), streamAction(streamUrl))
    }

    @Test
    fun `create call with conversation action required parameters only`() {
        testSingleNcco(mapOf("name" to conversationName), conversationAction(conversationName))
    }

    @Test
    fun `create call with input action required parameters only`() {
        testSingleNcco(ncco = inputAction())
    }

    @Test
    fun `create call with notify action required parameters only`() {
        testSingleNcco(
            mapOf("eventUrl" to listOf(eventUrl), "payload" to mapOf<String, Any>()),
            notifyAction(eventUrl, mapOf())
        )
    }

    @Test
    fun `create call with record action required parameters only`() {
        testSingleNcco(ncco = recordAction())
    }

    @Test
    fun `create call with single connect action and builder properties`() {
        testSingleNcco(
            mapOf(
                "endpoint" to listOf(mapOf("type" to phoneType, "number" to altNumber)),
                "eventUrl" to listOf(eventUrl), "ringbackTone" to ringbackTone
            ),
            connectAction(com.vonage.client.voice.ncco.PhoneEndpoint.builder(altNumber).build()) {
                eventUrl(eventUrl); ringbackTone(ringbackTone)
            }
        )
    }

    @Test
    fun `create call with single connect action no properties`() {
        testSingleNcco(
            mapOf(
                "endpoint" to listOf(mapOf("type" to phoneType, "number" to altNumber)),
            ),
            connectAction(com.vonage.client.voice.ncco.PhoneEndpoint.builder(altNumber).build())
        )
    }

    @Test
    fun `create call with connect to VBC ncco`() {
        testSingleNccoConnect(
            mapOf("extension" to vbcExt),
            connectToVbc(vbcExt)
        )
    }

    @Test
    fun `create call with connect to App ncco`() {
        testSingleNccoConnect(
            mapOf("user" to user),
            connectToApp(user)
        )
    }

    @Test
    fun `create call with connect to PSTN ncco`() {
        testSingleNccoConnect(
            mapOf("number" to toNumber),
            connectToPstn(toNumber)
        )

        testSingleNccoConnect(
            mapOf(
                "number" to toNumber, "dtmfAnswer" to dtmf,
                "onAnswer" to mapOf("url" to onAnswerUrl, "ringback" to ringbackTone)
            ),
            connectToPstn(toNumber, dtmf, onAnswerUrl, ringbackTone)
        )
    }

    @Test
    fun `create call with connect to WebSocket ncco`() {
        testSingleNccoConnect(
            mapOf("uri" to websocketUri, "content-type" to wsContentType),
            connectToWebsocket(websocketUri, wsContentType)
        )

        testSingleNccoConnect(
            mapOf("uri" to websocketUri, "content-type" to wsContentType, "headers" to customHeaders),
            connectToWebsocket(websocketUri, wsContentType, customHeaders)
        )
    }

    @Test
    fun `create call with connect to SIP ncco`() {
        testSingleNccoConnect(
            mapOf("uri" to sipUri),
            connectToSip(sipUri)
        )

        testSingleNccoConnect(
            mapOf("uri" to sipUri, "headers" to customHeaders,
                "standardHeaders" to mapOf("User-to-User" to userToUserHeader)
            ),
            connectToSip(sipUri, customHeaders, userToUserHeader)
        )
    }

    @Test
    fun `create call with all NCCO actions required parameters and empty builder properties`() {
        val emptyMap = mapOf<String, Any>()
        
        testCreateCall(mapOf(
            "random_from_number" to true,
            "to" to listOf(mapOf(
                "type" to "vbc",
                "extension" to vbcExt
            )),
            "advanced_machine_detection" to emptyMap,
            "ncco" to listOf(
                mapOf(
                    "action" to "talk",
                    "text" to text
                ),
                mapOf(
                    "action" to "stream",
                    "streamUrl" to listOf(streamUrl)
                ),
                mapOf(
                    "action" to "conversation",
                    "name" to conversationName,
                    "record" to true,
                    "transcription" to emptyMap
                ),
                mapOf(
                    "action" to "input",
                    "speech" to emptyMap,
                    "dtmf" to emptyMap
                ),
                mapOf(
                    "action" to "notify",
                    "eventUrl" to listOf(eventUrl),
                    "payload" to emptyMap
                ),
                mapOf(
                    "action" to "record",
                    "transcription" to emptyMap
                ),
                mapOf(
                    "action" to "connect",
                    "endpoint" to listOf(mapOf(
                        "type" to "sip",
                        "uri" to sipUri
                    )),
                    "advancedMachineDetection" to emptyMap
                )
            )
        )) {
            fromRandomNumber(true); to(com.vonage.client.voice.VbcEndpoint(vbcExt))
            advancedMachineDetection(); ncco(
                talkAction(text),
                streamAction(streamUrl),
                conversationAction(conversationName) {
                    transcription(); record(true)
                },
                inputAction {
                    speech(); dtmf()
                },
                notifyAction(eventUrl, mapOf()),
                recordAction {
                    transcription()
                },
                connectAction(com.vonage.client.voice.ncco.SipEndpoint.builder(sipUri).build()) {
                    advancedMachineDetection()
                }
            )
        }
    }

    @Test
    fun `create call with all NCCO actions and all parameters of those actions`() {
        val bargeIn = false
        val premium = true
        val loop = 2
        val style = 1
        val level = -0.5f
        val canHearId = UUID.randomUUID().toString()
        val canSpeakId = UUID.randomUUID().toString()
        val conversationEventMethod = EventMethod.POST
        val transcriptionEventMethod = EventMethod.GET
        val notifyEventMethod = conversationEventMethod
        val recordEventMethod = transcriptionEventMethod
        val inputEventMethod = transcriptionEventMethod
        val connectEventMethod = conversationEventMethod
        val musicOnHoldUrl = "https://nexmo-community.github.io/ncco-examples/assets/voice_api_audio_streaming.mp3"
        val transcriptionEventUrl = "https://example.com/transcription"
        val mute = true
        val record = true
        val endOnExit = true
        val startOnEnter = true
        val inputActionTypes = listOf("dtmf", "asr")
        val dtmfTimeout = 7
        val maxDigits = 16
        val submitOnHash = true
        val endOnSilenceSpeech = 4.6
        val maxDuration = 48
        val startTimeout = 23
        val sensitivity = 51
        val saveAudio = true
        val speechUuid = canSpeakId
        val speechContext = listOf("sales", "support", "customer", "Developer")
        val recordingTimeout = 1260
        val recordingChannels = 18
        val limit = 5710
        val endOnKey = 'x'
        val endOnSilenceRecording = 7
        val splitRecording = SplitRecording.CONVERSATION
        val recordEventUrl = "https://example.com/recordings"
        val beepStart = true
        val machineDetection = MachineDetection.CONTINUE
        val eventType = EventType.SYNCHRONOUS
        val connectTimeout = 38
        val beepTimeout = 90

        testCreateCall(mapOf(
            "from" to mapOf(
                "type" to phoneType,
                "number" to fromPstn
            ),
            "to" to listOf(
                mapOf(
                    "type" to phoneType,
                    "number" to toNumber
                )
            ),
            "machine_detection" to machineDetection.name.lowercase(),
            "ncco" to listOf(
                mapOf(
                    "action" to "talk",
                    "text" to text,
                    "language" to "te-IN",
                    "premium" to premium,
                    "loop" to loop,
                    "level" to level,
                    "style" to style,
                    "bargeIn" to bargeIn
                ),
                mapOf(
                    "action" to "stream",
                    "streamUrl" to listOf(streamUrl),
                    "level" to level,
                    "bargeIn" to bargeIn,
                    "loop" to loop
                ),
                mapOf(
                    "action" to "conversation",
                    "name" to conversationName,
                    "startOnEnter" to startOnEnter,
                    "endOnExit" to endOnExit,
                    "record" to record,
                    "mute" to mute,
                    "eventMethod" to conversationEventMethod.name,
                    "musicOnHoldUrl" to listOf(musicOnHoldUrl),
                    "eventUrl" to listOf(eventUrl),
                    "canSpeak" to listOf(canSpeakId, testUuidStr),
                    "canHear" to listOf(canHearId, testUuidStr),
                    "transcription" to mapOf(
                        "language" to "es-DO",
                        "eventUrl" to listOf(transcriptionEventUrl),
                        "eventMethod" to transcriptionEventMethod.name,
                        "sentimentAnalysis" to true
                    )
                ),
                mapOf(
                    "action" to "input",
                    "type" to inputActionTypes,
                    "eventUrl" to listOf(eventUrl),
                    "eventMethod" to inputEventMethod.name,
                    "speech" to mapOf(
                        "uuid" to listOf(speechUuid),
                        "context" to speechContext,
                        "endOnSilence" to endOnSilenceSpeech,
                        "startTimeout" to startTimeout,
                        "maxDuration" to maxDuration,
                        "sensitivity" to sensitivity,
                        "saveAudio" to saveAudio,
                        "language" to "uk-UA"
                    ),
                    "dtmf" to mapOf(
                        "timeOut" to dtmfTimeout,
                        "maxDigits" to maxDigits,
                        "submitOnHash" to submitOnHash
                    )
                ),
                mapOf(
                    "action" to "notify",
                    "eventUrl" to listOf(eventUrl),
                    "eventMethod" to notifyEventMethod.name,
                    "payload" to customHeaders
                ),
                mapOf(
                    "action" to "record",
                    "timeOut" to recordingTimeout,
                    "eventUrl" to listOf(recordEventUrl),
                    "eventMethod" to recordEventMethod.name,
                    "endOnSilence" to endOnSilenceRecording,
                    "endOnKey" to endOnKey,
                    "beepStart" to beepStart,
                    "channels" to recordingChannels,
                    "split" to splitRecording.name.lowercase(),
                    "transcription" to mapOf(
                        "language" to "en-ZA",
                        "eventUrl" to listOf(eventUrl),
                        "eventMethod" to transcriptionEventMethod.name,
                        "sentimentAnalysis" to false
                    )
                ),
                mapOf(
                    "action" to "connect",
                    "eventUrl" to listOf(eventUrl),
                    "eventMethod" to connectEventMethod.name,
                    "eventType" to eventType.name.lowercase(),
                    "limit" to limit,
                    "randomFromNumber" to true,
                    "ringbackTone" to ringbackTone,
                    "timeout" to connectTimeout,
                    "advancedMachineDetection" to mapOf(
                        "beep_timeout" to beepTimeout
                    ),
                    "endpoint" to listOf(
                        mapOf(
                            "type" to phoneType,
                            "number" to altNumber,
                            "dtmfAnswer" to dtmf
                        )
                    )
                )
            )

        )) {
            to(com.vonage.client.voice.PhoneEndpoint(toNumber))
            machineDetection(machineDetection)
            from(fromPstn); ncco(
                talkAction(text) {
                    language(TextToSpeechLanguage.TELUGU)
                    bargeIn(bargeIn); premium(premium)
                    style(style); loop(loop); level(level)
                },
                streamAction(streamUrl) {
                    loop(loop); level(level); bargeIn(bargeIn)
                },
                conversationAction(conversationName) {
                    addCanHear(canHearId); addCanSpeak(canSpeakId)
                    addCanHear(testUuidStr); addCanSpeak(testUuidStr)
                    eventMethod(conversationEventMethod); eventUrl(eventUrl)
                    musicOnHoldUrl(musicOnHoldUrl); record(record); mute(mute);
                    startOnEnter(startOnEnter); endOnExit(endOnExit)
                    transcription {
                        eventMethod(transcriptionEventMethod); eventUrl(transcriptionEventUrl)
                        language(SpeechSettings.Language.SPANISH_DOMINICAN_REPUBLIC);
                        sentimentAnalysis(true)
                    }
                },
                inputAction {
                    eventUrl(eventUrl); eventMethod(inputEventMethod)
                    type(inputActionTypes); speech {
                        uuid(speechUuid); context(speechContext)
                        language(SpeechSettings.Language.UKRAINIAN)
                        endOnSilence(endOnSilenceSpeech); maxDuration(maxDuration)
                        sensitivity(sensitivity); startTimeout(startTimeout)
                        saveAudio(saveAudio);
                    }
                    dtmf(timeout = dtmfTimeout, maxDigits = maxDigits, submitOnHash = submitOnHash)
                },
                notifyAction(eventUrl, customHeaders, notifyEventMethod),
                recordAction {
                    timeOut(recordingTimeout); channels(recordingChannels)
                    endOnKey(endOnKey); endOnSilence(endOnSilenceRecording)
                    eventUrl(recordEventUrl); eventMethod(recordEventMethod)
                    split(splitRecording); beepStart(beepStart); transcription {
                        language(SpeechSettings.Language.ENGLISH_SOUTH_AFRICA)
                        eventUrl(eventUrl); eventMethod(transcriptionEventMethod)
                        sentimentAnalysis(false)
                    }
                },
                connectAction(com.vonage.client.voice.ncco.PhoneEndpoint.builder(altNumber).dtmfAnswer(dtmf).build()) {
                    eventUrl(eventUrl); eventMethod(connectEventMethod); limit(limit)
                    eventType(eventType); timeOut(connectTimeout); ringbackTone(ringbackTone);
                    randomFromNumber(true); advancedMachineDetection {
                        beepTimeout(beepTimeout)
                    }
                }
            )
        }
    }
}