package com.vonage.client.kt

import com.vonage.client.users.channels.Websocket.ContentType
import com.vonage.client.voice.*
import com.vonage.client.voice.ncco.*
import java.net.URI
import java.time.Instant
import java.util.*

class Voice(private val voiceClient: VoiceClient) {

    fun call(callId: String): ExistingCall = ExistingCall(callId)

    fun call(callId: UUID): ExistingCall = call(callId.toString())

    inner class ExistingCall(val callId: String) {

        fun info(): CallInfo = voiceClient.getCallDetails(callId)

        fun hangup() = voiceClient.terminateCall(callId)

        fun mute() = voiceClient.muteCall(callId)

        fun unmute() = voiceClient.unmuteCall(callId)

        fun earmuff() = voiceClient.earmuffCall(callId)

        fun unearmuff() = voiceClient.unearmuffCall(callId)

        fun transfer(vararg actions: Action) = voiceClient.transferCall(callId, Ncco(actions.asList()))

        fun transfer(nccoUrl: String) = voiceClient.transferCall(callId, nccoUrl)

        fun transfer(nccoUrl: URI) = transfer(nccoUrl.toString())

        fun sendDtmf(digits: String): DtmfResponse = voiceClient.sendDtmf(callId, digits)

        fun streamAudio(streamUrl: String, loop: Int = 1, level: Double = 0.0): StreamResponse =
            voiceClient.startStream(callId, streamUrl, loop, level)

        fun stopStream(): StreamResponse = voiceClient.stopStream(callId)

        fun startTalk(text: String, properties: (TalkPayload.Builder.() -> Unit) = {}): TalkResponse =
            voiceClient.startTalk(callId, TalkPayload.builder(text).apply(properties).build())

        fun stopTalk(): TalkResponse = voiceClient.stopTalk(callId)
    }

    fun listCalls(filter: (CallsFilter.Builder.() -> Unit)? = null): CallInfoPage =
        if (filter == null) voiceClient.listCalls()
        else voiceClient.listCalls(CallsFilter.builder().apply(filter).build())

    fun createCall(call: Call.Builder.() -> Unit): CallEvent =
        voiceClient.createCall(Call.builder().apply(call).build())
}

fun CallsFilter.Builder.dateStart(dateStart: String): CallsFilter.Builder =
    dateStart(Date.from(Instant.parse(dateStart)))

fun CallsFilter.Builder.dateEnd(dateEnd: String): CallsFilter.Builder =
    dateEnd(Date.from(Instant.parse(dateEnd)))

fun Call.Builder.advancedMachineDetection(amd: AdvancedMachineDetection.Builder.() -> Unit = {}): Call.Builder =
    advancedMachineDetection(AdvancedMachineDetection.builder().apply(amd).build())

fun InputAction.Builder.speech(settings: SpeechSettings.Builder.() -> Unit = {}): InputAction.Builder =
    speech(SpeechSettings.builder().apply(settings).build())

fun InputAction.Builder.dtmf(timeout: Int? = null, maxDigits: Int? = null, submitOnHash: Boolean? = null): InputAction.Builder {
    val dtmfSettings = DtmfSettings()
    dtmfSettings.timeOut = timeout
    dtmfSettings.maxDigits = maxDigits
    dtmfSettings.isSubmitOnHash = submitOnHash
    return dtmf(dtmfSettings)
}

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
