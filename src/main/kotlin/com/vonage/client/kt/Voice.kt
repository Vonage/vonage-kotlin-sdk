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

import com.vonage.client.voice.*
import com.vonage.client.voice.ncco.*
import java.net.URI
import java.time.Instant
import java.util.*

class Voice internal constructor(private val client: VoiceClient) {

    fun call(callId: String): ExistingCall = ExistingCall(callId)

    inner class ExistingCall internal constructor(id: String): ExistingResource(id) {

        fun info(): CallInfo = client.getCallDetails(id)

        fun hangup(): Unit = client.terminateCall(id)

        fun mute(): Unit = client.muteCall(id)

        fun unmute(): Unit = client.unmuteCall(id)

        fun earmuff(): Unit = client.earmuffCall(id)

        fun unearmuff(): Unit = client.unearmuffCall(id)

        fun transfer(vararg actions: Action): Unit = client.transferCall(id, Ncco(actions.asList()))

        fun transfer(nccoUrl: String): Unit = client.transferCall(id, nccoUrl)

        fun transfer(nccoUrl: URI): Unit = transfer(nccoUrl.toString())

        fun sendDtmf(digits: String): DtmfResponse = client.sendDtmf(id, digits)

        fun streamAudio(streamUrl: String, loop: Int = 1, level: Double = 0.0): StreamResponse =
            client.startStream(id, streamUrl, loop, level)

        fun stopStream(): StreamResponse = client.stopStream(id)

        fun startTalk(text: String, properties: (TalkPayload.Builder.() -> Unit) = {}): TalkResponse =
            client.startTalk(id, TalkPayload.builder(text).apply(properties).build())

        fun stopTalk(): TalkResponse = client.stopTalk(id)
    }

    fun listCalls(filter: (CallsFilter.Builder.() -> Unit)? = null): CallInfoPage =
        if (filter == null) client.listCalls()
        else client.listCalls(CallsFilter.builder().apply(filter).build())

    fun createCall(call: Call.Builder.() -> Unit): CallEvent =
        client.createCall(Call.builder().apply(call).build())
}

fun CallsFilter.Builder.dateStart(dateStart: String): CallsFilter.Builder =
    dateStart(Date.from(Instant.parse(dateStart)))

fun CallsFilter.Builder.dateEnd(dateEnd: String): CallsFilter.Builder =
    dateEnd(Date.from(Instant.parse(dateEnd)))

fun Call.Builder.advancedMachineDetection(amd: AdvancedMachineDetection.Builder.() -> Unit = {}): Call.Builder =
    advancedMachineDetection(AdvancedMachineDetection.builder().apply(amd).build())

fun InputAction.Builder.speech(settings: SpeechSettings.Builder.() -> Unit = {}): InputAction.Builder =
    speech(SpeechSettings.builder().apply(settings).build())

fun InputAction.Builder.dtmf(settings: DtmfSettings.Builder.() -> Unit = {}): InputAction.Builder =
    dtmf(DtmfSettings.builder().apply(settings).build())

fun ConversationAction.Builder.transcription(settings: TranscriptionSettings.Builder.() -> Unit = {}):
        ConversationAction.Builder = transcription(TranscriptionSettings.builder().apply(settings).build())

fun RecordAction.Builder.transcription(settings: TranscriptionSettings.Builder.() -> Unit = {}): RecordAction.Builder =
    transcription(TranscriptionSettings.builder().apply(settings).build())

fun ConnectAction.Builder.advancedMachineDetection(amd: AdvancedMachineDetection.Builder.() -> Unit = {}):
        ConnectAction.Builder = advancedMachineDetection(AdvancedMachineDetection.builder().apply(amd).build())

fun recordAction(properties: RecordAction.Builder.() -> Unit = {}): RecordAction =
    RecordAction.builder().apply(properties).build()

fun talkAction(text: String, properties: TalkAction.Builder.() -> Unit = {}): TalkAction =
    TalkAction.builder(text).apply(properties).build()

fun streamAction(streamUrl: String, properties: StreamAction.Builder.() -> Unit = {}): StreamAction =
    StreamAction.builder(streamUrl).apply(properties).build()

fun notifyAction(eventUrl: String, payload: Map<String, Any>, eventMethod: EventMethod? = null): NotifyAction =
    NotifyAction.builder(payload, eventUrl).eventMethod(eventMethod).build()

fun inputAction(properties: InputAction.Builder.() -> Unit = {}): InputAction =
    InputAction.builder().apply(properties).build()

fun conversationAction(name: String, properties: ConversationAction.Builder.() -> Unit = {}): ConversationAction =
    ConversationAction.builder(name).apply(properties).build()

fun connectAction(endpoint: com.vonage.client.voice.ncco.Endpoint,
                  properties: ConnectAction.Builder.() -> Unit = {}): ConnectAction =
    ConnectAction.builder(endpoint).apply(properties).build()

fun connectToPstn(number: String, dtmfAnswer: String? = null,
                  onAnswerUrl: String? = null, onAnswerRingback: String? = null,
                  properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectAction(com.vonage.client.voice.ncco.PhoneEndpoint.builder(number)
        .dtmfAnswer(dtmfAnswer).onAnswer(onAnswerUrl, onAnswerRingback).build(), properties
    )

fun connectToVbc(extension: String, properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectAction(com.vonage.client.voice.ncco.VbcEndpoint.builder(extension).build(), properties)

fun connectToApp(user: String, properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectAction(com.vonage.client.voice.ncco.AppEndpoint.builder(user).build(), properties)

fun connectToWebsocket(uri: String, contentType: String, headers: Map<String, Any>? = null,
                       properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectAction(com.vonage.client.voice.ncco.WebSocketEndpoint.builder(uri, contentType)
        .headers(headers).build(), properties
    )

fun connectToSip(uri: String, customHeaders: Map<String, Any>? = null, userToUserHeader: String? = null,
                 properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction {
    val builder = com.vonage.client.voice.ncco.SipEndpoint.builder(uri)
    if (customHeaders != null) {
        builder.headers(customHeaders)
    }
    if (userToUserHeader != null) {
        builder.userToUserHeader(userToUserHeader)
    }
    return connectAction(builder.build(), properties)
}

fun Call.Builder.toPstn(number: String, dtmfAnswer: String? = null): Call.Builder =
    to(com.vonage.client.voice.PhoneEndpoint(number, dtmfAnswer))

fun Call.Builder.toSip(uri: String, customHeaders: Map<String, Any>? = null,
                       userToUserHeader: String? = null): Call.Builder =
    to(com.vonage.client.voice.SipEndpoint(uri, customHeaders, userToUserHeader))

fun Call.Builder.toWebSocket(uri: String, contentType: String? = null,
                             headers: Map<String, Any>? = null): Call.Builder =
    to(com.vonage.client.voice.WebSocketEndpoint(uri, contentType, headers))

fun Call.Builder.toVbc(extension: String): Call.Builder =
    to(com.vonage.client.voice.VbcEndpoint(extension))

fun Call.Builder.toApp(user: String): Call.Builder =
    to(com.vonage.client.voice.AppEndpoint(user))
