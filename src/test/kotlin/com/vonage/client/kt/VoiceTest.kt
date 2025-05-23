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

import com.vonage.client.common.HttpMethod
import com.vonage.client.common.SortOrder
import com.vonage.client.voice.*
import com.vonage.client.voice.ncco.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.util.*
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.test.*

class VoiceTest : AbstractTest() {
    private val client = vonage.voice
    private val callsBaseUrl = "/v1/calls"
    private val callUrl = "$callsBaseUrl/$callIdStr"
    private val existingCall = client.call(callIdStr)
    private val conversationId = "CON-f972836a-550f-45fa-956c-12a2ab5b7d22"
    private val price = 23.40
    private val duration = 60
    private val rate = 0.39
    private val phoneType = "phone"
    private val count = 89
    private val pageSize = 25
    private val recordIndex = 14
    private val sipDomainName = "my_example-domain"
    private val fromPstn = "14155550100"
    private val streamUrl = "$exampleUrlBase/waiting.mp3"
    private val onAnswerUrl = "$exampleUrlBase/ncco.json"
    private val ringbackTone = "https://example.org/ringbackTone.wav"
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
        assertEquals(EndpointType.PHONE, to.type)
        assertEquals(toNumber, (to as com.vonage.client.voice.PhoneEndpoint).number)
        val from = callInfo.from
        assertNotNull(from)
        assertEquals(EndpointType.PHONE, from.type)
        assertEquals(altNumber, (from as com.vonage.client.voice.PhoneEndpoint).number)
        assertEquals(CallStatus.COMPLETED, callInfo.status)
        assertEquals(CallDirection.INBOUND, callInfo.direction)
        assertEquals(rate, callInfo.rate)
        assertEquals(price, callInfo.price)
        assertEquals(duration, callInfo.duration)
        assertEquals(startTime, callInfo.startTime)
        assertEquals(endTime, callInfo.endTime)
        assertEquals(networkCode, callInfo.network)
    }

    private fun assertEqualsSampleCallsPage(callsPage: CallInfoPage) {
        assertNotNull(callsPage)
        assertEquals(pageSize, callsPage.pageSize)
        assertEquals(recordIndex, callsPage.recordIndex)
        assertEquals(count, callsPage.count)
        val infos = callsPage.callInfos
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
        invocation()
        assertExistingCall404(callUrl, HttpMethod.PUT, invocation)
    }

    private fun testStream(loop: Int = 1, level: Double = 0.0, invocation: (() -> StreamResponse)? = null) {

        val message = "Stream ${if (invocation == null) "stopped" else "started"}"
        val expectedResponseParams = mapOf("message" to message, "uuid" to callIdStr)
        val streamUrl = "$callUrl/stream"
        val response = if (invocation == null) {
            mockDelete(streamUrl, expectedResponseParams = expectedResponseParams)
            existingCall.stopStream()
        }
        else {
            mockPut(streamUrl, status = 200,
                expectedRequestParams = mapOf(
                    "stream_url" to listOf(this.streamUrl),
                    "loop" to loop, "level" to level
                ),
                expectedResponseParams = expectedResponseParams
            )
            invocation()
        }
        assertNotNull(response)
        assertEquals(message, response.message)
        assertEquals(callIdStr, response.uuid)

        if (invocation != null) assertExistingCall404(streamUrl, HttpMethod.PUT, invocation)
        else assertExistingCall404(streamUrl, HttpMethod.DELETE, existingCall::stopStream)
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
        val response = invocation()
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

        val callEvent = client.createCall(call)
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
            ncco(ncco); fromRandomNumber(true)
            to(com.vonage.client.voice.WebSocketEndpoint(websocketUri, null, null))
        }
    }

    private fun testSingleNccoConnect(params: Map<String, Any>, connectAction: ConnectAction) =
        testSingleNcco(mapOf("endpoint" to listOf(mapOf(
            "type" to connectAction.endpoint.first().type) + params)), connectAction
        )

    @Test
    fun `existing call equals and hashCode`() {
        val same = client.call(callIdStr)
        assertEquals(existingCall, same)
        assertEquals(existingCall.hashCode(), same.hashCode())
        assertEquals(callIdStr.hashCode(), existingCall.hashCode())
        assertFalse(existingCall.equals(callIdStr))
        assertFalse(existingCall.equals(null))
        val different = client.call(testUuidStr)
        assertNotEquals(existingCall, different)
        assertNotEquals(existingCall.hashCode(), different.hashCode())
    }

    @Test
    fun `existing call toString`() {
        assertEquals("com.vonage.client.kt.Voice.ExistingCall {id=$callIdStr}", existingCall.toString())
    }

    @Test
    fun `terminate call`() {
        testModifyCall("hangup", existingCall::hangup)
    }

    @Test
    fun `mute call`() {
        testModifyCall("mute", existingCall::mute)
    }

    @Test
    fun `umute call`() {
        testModifyCall("unmute", existingCall::unmute)
    }

    @Test
    fun `earmuff call`() {
        testModifyCall("earmuff", existingCall::earmuff)
    }

    @Test
    fun `umearmuff call`() {
        testModifyCall("unearmuff", existingCall::unearmuff)
    }

    @Test
    fun `get call`() {
        mockGet(expectedUrl = callUrl, expectedResponseParams = callResponseMap)
        assertEqualsSampleCall(existingCall.info())
    }

    @Test
    fun `transfer call with answer url`() {
        testModifyCall(nccoUrl = onAnswerUrl, invocation = {
            existingCall.transfer(onAnswerUrl)
        })
    }

    @Test
    fun `transfer call with ncco`() {
        testModifyCall(nccoAction = mapOf("action" to "talk", "text" to text), invocation = {
            existingCall.transfer(TalkAction.builder(text).build())
        })
    }

    @Test
    fun `send dtmf`() {
        val message = "DTMF sent"
        mockPut(expectedUrl = "$callUrl/dtmf", status = 200,
            expectedRequestParams = mapOf("digits" to dtmf),
            expectedResponseParams = mapOf("message" to message, "uuid" to callIdStr)
        )
        val response = existingCall.sendDtmf(dtmf)
        assertNotNull(response)
        assertEquals(message, response.message)
        assertEquals(callIdStr, response.uuid)
    }

    @Test
    fun `subscribe to real-time DTMF events`() {
        mockPut(expectedUrl = "$callUrl/input/dtmf", status = 200,
            expectedRequestParams = mapOf("event_url" to listOf(eventUrl))
        )
        existingCall.subscribeToDtmfEvents(eventUrl)
    }

    @Test
    fun `stop receiving real-time DTMF events`() {
        mockDelete(expectedUrl = "$callUrl/input/dtmf")
        existingCall.stopAsyncDtmfEvents()
    }

    @Test
    fun `stream audio into call`() {
        val loop = 3
        val level = 0.45
        testStream {
            existingCall.streamAudio(streamUrl)
        }
        testStream(loop = loop) {
            existingCall.streamAudio(streamUrl, loop)
        }
        testStream(level = level) {
            existingCall.streamAudio(streamUrl, level = level)
        }
        testStream(loop = loop, level = level) {
            existingCall.streamAudio(streamUrl, loop = loop, level = level)
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
            existingCall.startTalk(text) {
                language(TextToSpeechLanguage.SCOTTISH_ENGLISH)
                style(style); premium(premium); loop(loop); level(level)
            }
        }
    }

    @Test
    fun `play text to speech text only`() {
        testTextToSpeech(mapOf("text" to text)) {
            existingCall.startTalk(text)
        }
    }

    @Test
    fun `stop text to speech`() {
        testTextToSpeech {
            existingCall.stopTalk()
        }
    }

    @Test
    fun `list calls all filter parameters`() {
        mockGet(callsBaseUrl, expectedQueryParams = mapOf(
            "status" to "unanswered",
            "date_start" to startTimeStr,
            "date_end" to endTimeStr,
            "page_size" to pageSize,
            "record_index" to recordIndex,
            "order" to "desc",
            "conversation_uuid" to conversationId
        ), expectedResponseParams = listCallsResponse)

        val callsPage = client.listCalls {
            status(CallStatus.UNANSWERED)
            startDate(startTime); endDate(endTime)
            pageSize(pageSize); recordIndex(recordIndex)
            order(SortOrder.DESCENDING); conversationUuid(conversationId)
        }

        assertEqualsSampleCallsPage(callsPage)
    }

    @Test
    fun `list calls no filter`() {
        mockGet(callsBaseUrl, expectedResponseParams = listCallsResponse)
        assertEqualsSampleCallsPage(client.listCalls())
    }

    @Test
    fun `download recording to temp file`() {
        val fileName = "$randomUuidStr.wav"
        val relativeUrl = "/api.nexmo.com/v1/files/$fileName"
        val recordingUrl = wmBaseUrl + relativeUrl
        var tempFile = Files.createTempFile(null, null)
        tempFile.deleteExisting()
        val content = "<110B1NARY0101;>".toByteArray(Charsets.UTF_8)
        mockGetBinary(relativeUrl, content)

        client.downloadRecording(recordingUrl, tempFile)
        assertArrayEquals(content, tempFile.readBytes())
        tempFile.deleteExisting()
        assertFalse(tempFile.exists())

        client.downloadRecording(recordingUrl, tempFile.parent)
        tempFile = tempFile.parent.resolve(fileName)
        assertTrue(tempFile.exists())
        assertArrayEquals(content, tempFile.readBytes())
        tempFile.deleteExisting()
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
        testCreateCallToSingleEndpoint(mapOf("type" to "app", "user" to userName)) {
            toApp(userName)
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
        val baseMap = mapOf("type" to "websocket", "uri" to websocketUri, "content-type" to wsContentTypeStr)
        testCreateCallToSingleEndpoint(baseMap) {
            toWebSocket(websocketUri, wsContentType)
        }

        testCreateCallToSingleEndpoint(baseMap + mapOf(
            "content-type" to wsContentTypeStr, "headers" to customHeaders
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
        val answerMethod = EventMethod.GET
        val eventMethod = EventMethod.POST
        val amdBehaviour = MachineDetection.HANGUP
        val amdMode = AdvancedMachineDetection.Mode.DETECT_BEEP

        testCreateCall(mapOf(
            "answer_url" to listOf(answerUrl),
            "answer_method" to answerMethod.name,
            "to" to listOf(
                mapOf(
                    "type" to "app",
                    "user" to userName
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
                    "content-type" to wsContentTypeStr,
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
            from(fromPstn); fromRandomNumber(false)
            eventUrl(eventUrl); eventMethod(eventMethod)
            lengthTimer(lengthTimer); ringingTimer(ringingTimer)
            advancedMachineDetection {
                behavior(amdBehaviour); beepTimeout(beepTimeout); mode(amdMode)
            }
            to(
                com.vonage.client.voice.AppEndpoint(userName),
                com.vonage.client.voice.PhoneEndpoint(toNumber, dtmf),
                com.vonage.client.voice.VbcEndpoint(vbcExt),
                com.vonage.client.voice.WebSocketEndpoint(websocketUri, wsContentTypeStr, customHeaders),
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
        val emptySpeech = mapOf("speech" to emptyMap<String, Any>())
        testSingleNcco(
            additionalParams = mapOf("type" to listOf("dtmf", "speech")) + emptySpeech,
            ncco = inputAction { dtmf(); speech() }
        )
        testSingleNcco(
            additionalParams = mapOf("type" to listOf("dtmf")),
            ncco = inputAction { dtmf() }
        )
        testSingleNcco(
            additionalParams = mapOf("type" to listOf("speech")) + emptySpeech,
            ncco = inputAction { speech() }
        )
    }

    @Test
    fun `at least one type should be specified in inputAction`() {
        assertThrows<IllegalStateException> {
            inputAction { }
        }
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
            mapOf("user" to userName),
            connectToApp(userName)
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
            mapOf("uri" to websocketUri, "content-type" to wsContentTypeStr),
            connectToWebsocket(websocketUri, wsContentType)
        )

        testSingleNccoConnect(
            mapOf("uri" to websocketUri, "content-type" to wsContentTypeStr, "headers" to customHeaders),
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
            mapOf("domain" to sipDomainName),
            connectToSip(sipDomainName, user = null)
        )

        testSingleNccoConnect(
            mapOf("domain" to sipDomainName, "user" to userName),
            connectToSip(sipDomainName, userName)
        )

        testSingleNccoConnect(
            mapOf("uri" to sipUri, "headers" to customHeaders,
                "standardHeaders" to mapOf("User-to-User" to userToUserHeader)
            ),
            connectToSip(sipUri, customHeaders, userToUserHeader)
        )

        testSingleNccoConnect(
            mapOf("domain" to sipDomainName, "user" to userName, "headers" to customHeaders,
                "standardHeaders" to mapOf("User-to-User" to userToUserHeader)
            ),
            connectToSip(sipDomainName, userName, customHeaders, userToUserHeader)
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
                    "type" to listOf("speech", "dtmf"),
                    "speech" to emptyMap
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
        val level = -0.5
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
        val inputActionTypes = listOf("speech", "dtmf")
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
                    musicOnHoldUrl(musicOnHoldUrl); record(record); mute(mute)
                    startOnEnter(startOnEnter); endOnExit(endOnExit)
                    transcription {
                        eventMethod(transcriptionEventMethod); eventUrl(transcriptionEventUrl)
                        language(Language.SPANISH_DOMINICAN_REPUBLIC)
                        sentimentAnalysis(true)
                    }
                },
                inputAction {
                    eventUrl(eventUrl); eventMethod(inputEventMethod)
                    speech {
                        uuid(speechUuid); context(speechContext); saveAudio(saveAudio)
                        language(Language.UKRAINIAN)
                        endOnSilence(endOnSilenceSpeech); maxDuration(maxDuration)
                        sensitivity(sensitivity); startTimeout(startTimeout)
                    }
                    dtmf {
                        timeOut(dtmfTimeout); maxDigits(maxDigits); submitOnHash(submitOnHash)
                    }
                },
                notifyAction(eventUrl, customHeaders, notifyEventMethod),
                recordAction {
                    timeOut(recordingTimeout); channels(recordingChannels)
                    endOnKey(endOnKey); endOnSilence(endOnSilenceRecording)
                    eventUrl(recordEventUrl); eventMethod(recordEventMethod)
                    beepStart(beepStart); transcription {
                        language(Language.ENGLISH_SOUTH_AFRICA)
                        eventUrl(eventUrl); eventMethod(transcriptionEventMethod)
                        sentimentAnalysis(false)
                    }
                },
                connectAction(com.vonage.client.voice.ncco.PhoneEndpoint.builder(altNumber).dtmfAnswer(dtmf).build()) {
                    eventUrl(eventUrl); eventMethod(connectEventMethod); limit(limit)
                    eventType(eventType); timeOut(connectTimeout); ringbackTone(ringbackTone)
                    randomFromNumber(true); advancedMachineDetection {
                        beepTimeout(beepTimeout)
                    }
                }
            )
        }
    }
}