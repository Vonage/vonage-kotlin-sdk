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

import com.vonage.client.users.channels.Websocket
import com.vonage.client.voice.*
import com.vonage.client.voice.ncco.*
import java.nio.file.Path

/**
 * Implementation of the [Voice API](https://developer.vonage.com/en/api/voice).
 *
 * *Authentication method:* JWT.
 */
class Voice internal constructor(private val client: VoiceClient) {

    /**
     * Call this method to work with an existing call.
     *
     * @param callId UUID of the call to work with.
     */
    fun call(callId: String): ExistingCall = ExistingCall(callId)

    /**
     * Class for working with an existing call.
     *
     * @property id The call ID.
     */
    inner class ExistingCall internal constructor(id: String): ExistingResource(id) {

        /**
         * Get information about the call.
         *
         * @return Details of the call.
         */
        fun info(): CallInfo = client.getCallDetails(id)

        /**
         * End the call.
         */
        fun hangup(): Unit = client.terminateCall(id)

        /**
         * Mute the call. The other party will not be able to hear this call.
         */
        fun mute(): Unit = client.muteCall(id)

        /**
         * Unmute the call. The other party will be able to hear this call again.
         */
        fun unmute(): Unit = client.unmuteCall(id)

        /**
         * Earmuff the call. This call will not be able to hear audio.
         */
        fun earmuff(): Unit = client.earmuffCall(id)

        /**
         * Unearmuff the call. This call will be able to hear audio again.
         */
        fun unearmuff(): Unit = client.unearmuffCall(id)

        /**
         * Transfer the call using an NCCO.
         *
         * @param actions The actions to perform after the transfer.
         */
        fun transfer(vararg actions: Action): Unit = client.transferCall(id, Ncco(actions.asList()))

        /**
         * Transfer the call using an answer URL.
         *
         * @param nccoUrl URL of the endpoint that will return the NCCO to execute after the transfer.
         */
        fun transfer(nccoUrl: String): Unit = client.transferCall(id, nccoUrl)

        /**
         * Play DTMF tones into the call.
         *
         * @param digits The digits to send. Valid characters are `0-9`, `#`, `*` and `p`,
         * which indicates a short pause between tones.
         *
         * @return The DTMF status and call leg ID.
         */
        fun sendDtmf(digits: String): DtmfResponse = client.sendDtmf(id, digits)

        /**
         * Subscribe to real-time DTMF key presses on the call, where the input mode is [InputMode.ASYNCHRONOUS].
         *
         * @param eventUrl The publicly accessible URL to send DTMF events to,
         * which can be deserialised using [EventWebhook.fromJson].
         *
         * @since 1.1.0
         */
        fun subscribeToDtmfEvents(eventUrl: String): Unit = client.addDtmfListener(id, eventUrl)

        /**
         * Stop asynchronous DTMF events being sent to the URL configured in [subscribeToDtmfEvents].
         *
         * @since 1.1.0
         */
        fun stopAsyncDtmfEvents(): Unit = client.removeDtmfListener(id)

        /**
         * Play an audio file to the call.
         *
         * @param url The publicly accessible URL of the audio file to play.
         * @param loop The number of times to loop the audio file.
         * @param level The volume level at which to play the audio file, between -1 (quietest) and 1 (loudest).
         *
         * @return The stream status and call leg ID.
         */
        fun streamAudio(streamUrl: String, loop: Int = 1, level: Double = 0.0): StreamResponse =
            client.startStream(id, streamUrl, loop, level)

        /**
         * Stop playing audio into the call.
         *
         * @return The stream status and call leg ID.
         */
        fun stopStream(): StreamResponse = client.stopStream(id)

        /**
         * Speak text into the call.
         *
         * @param text The text to speak.
         * @param properties (OPTIONAL) Additional properties for the talk action.
         *
         * @return The talk status and call leg ID.
         */
        fun startTalk(text: String, properties: (TalkPayload.Builder.() -> Unit) = {}): TalkResponse =
            client.startTalk(id, TalkPayload.builder(text).apply(properties).build())

        /**
         * Stop the Text-to-Speech (TTS).
         *
         * @return The talk status and call leg ID.
         */
        fun stopTalk(): TalkResponse = client.stopTalk(id)
    }

    /**
     * Retrieve details of your calls.
     *
     * @param filter (OPTIONAL) A lambda function for specifying the parameters to narrow down the results.
     */
    fun listCalls(filter: (CallsFilter.Builder.() -> Unit)? = null): CallInfoPage =
        if (filter == null) client.listCalls()
        else client.listCalls(CallsFilter.builder().apply(filter).build())

    /**
     * Initiate an outbound call.
     *
     * @param properties A lambda function for specifying the call's parameters.
     *
     * @return Details of the created call.
     */
    fun createCall(properties: Call.Builder.() -> Unit): CallEvent =
        client.createCall(Call.builder().apply(properties).build())

    /**
     * Download a recording of a call and save it to a file.
     *
     * @param recordingUrl The URL of the recording to download.
     * @param destination Absolute path to save the recording to.
     */
    fun downloadRecording(recordingUrl: String, destination: Path): Unit =
        client.saveRecording(recordingUrl, destination)
}

/**
 * Configure the behavior of Advanced Machine Detection. This overrides [Call.Builder#machineDetection] setting
 * and is a premium feature, so you cannot set both.
 *
 * @param amd A lambda function for configuring the Advanced Machine Detection settings.
 *
 * @return The updated [Call.Builder].
 */
fun Call.Builder.advancedMachineDetection(amd: AdvancedMachineDetection.Builder.() -> Unit = {}): Call.Builder =
    advancedMachineDetection(AdvancedMachineDetection.builder().apply(amd).build())

/**
 * Configure the behavior of Automatic Speech Recognition to enable speech input. This setting is mutually
 * exclusive with [Call.Builder#dtmf], so you must provide one or the other for receiving input from the callee.
 *
 * @param settings (OPTIONAL) A lambda function for configuring the speech recognition settings.
 *
 * @return The updated [InputAction.Builder].
 */
fun InputAction.Builder.speech(settings: SpeechSettings.Builder.() -> Unit = {}): InputAction.Builder =
    speech(SpeechSettings.builder().apply(settings).build())

/**
 * Configure the behavior of Dual-Tone Multi-Frequency (DTMF) input.
 *
 * @param settings A lambda function for configuring the DTMF settings. If you provide this instead of
 * calling the [InputAction.Builder#dtmf()] method, you cannot use asynchronous input mode.
 *
 * @return The updated [InputAction.Builder].
 */
fun InputAction.Builder.dtmf(settings: DtmfSettings.Builder.() -> Unit): InputAction.Builder =
    dtmf(DtmfSettings.builder().apply(settings).build())

/**
 * Configure the behaviour of call recording transcription. If present (even if all settings are default),
 * transcription is activated. The [ConversationAction.Builder.record] parameter must also be set to `true`.
 *
 * @param settings (OPTIONAL) A lambda function for configuring the transcription settings.
 *
 * @return The updated [ConversationAction.Builder].
 */
fun ConversationAction.Builder.transcription(settings: TranscriptionSettings.Builder.() -> Unit = {}):
        ConversationAction.Builder = transcription(TranscriptionSettings.builder().apply(settings).build())

/**
 * Configure the behaviour of call recording transcription. Calling this method activates transcription.
 *
 * @param settings (OPTIONAL) A lambda function for configuring the transcription settings.
 *
 * @return The updated [RecordAction.Builder].
 */
fun RecordAction.Builder.transcription(settings: TranscriptionSettings.Builder.() -> Unit = {}): RecordAction.Builder =
    transcription(TranscriptionSettings.builder().apply(settings).build())

/**
 * Configure the behavior of Advanced Machine Detection. This overrides [ConnectAction.Builder#machineDetection]
 * setting and is a premium feature, so you cannot set both.
 *
 * @param amd A lambda function for configuring the Advanced Machine Detection settings.
 *
 * @return The updated [ConnectAction.Builder].
 */
fun ConnectAction.Builder.advancedMachineDetection(amd: AdvancedMachineDetection.Builder.() -> Unit = {}):
        ConnectAction.Builder = advancedMachineDetection(AdvancedMachineDetection.builder().apply(amd).build())

/**
 * Builds an NCCO action to record the call.
 *
 * @param properties (OPTIONAL) A lambda function for configuring parameters of the record action.
 *
 * @return A new [RecordAction] with the specified properties.
 */
fun recordAction(properties: RecordAction.Builder.() -> Unit = {}): RecordAction =
    RecordAction.builder().apply(properties).build()

/**
 * Builds an NCCO action to play Text-to-Speech into the call.
 *
 * @param text A string of up to 1,500 characters (excluding SSML tags) containing the message to be synthesized in
 * the Call or Conversation. A single comma in text adds a short pause to the synthesized speech. To add a longer
 * pause a break tag needs to be used in SSML. To use SSML tags, you must enclose the text in a `speak` element.
 *
 * @param properties (OPTIONAL) A lambda function for configuring additional parameters of the TTS action.
 *
 * @return A new [TalkAction] with the specified properties.
 */
fun talkAction(text: String, properties: TalkAction.Builder.() -> Unit = {}): TalkAction =
    TalkAction.builder(text).apply(properties).build()

/**
 * Builds an NCCO action to play an audio stream into the call.
 *
 * @param streamUrl The URL of the audio stream to play.
 *
 * @param properties (OPTIONAL) A lambda function for configuring additional parameters of the stream action.
 *
 * @return A new [StreamAction] with the specified properties.
 */
fun streamAction(streamUrl: String, properties: StreamAction.Builder.() -> Unit = {}): StreamAction =
    StreamAction.builder(streamUrl).apply(properties).build()

/**
 * Builds an NCCO action to send custom events to a configured webhook.
 *
 * @param eventUrl The URL to send events to. If you return an NCCO when you receive a notification,
 * it will replace the current NCCO.
 *
 * @param payload A map of key-value pairs to send as the event payload.
 *
 * @param eventMethod (OPTIONAL) The HTTP method to use when sending the event.
 *
 * @return A new [NotifyAction] with the specified properties.
 */
fun notifyAction(eventUrl: String, payload: Map<String, Any>, eventMethod: EventMethod? = null): NotifyAction =
    NotifyAction.builder(payload, eventUrl).eventMethod(eventMethod).build()

/**
 * Builds an NCCO action for gathering input from the call via DTMF or speech recognition.
 *
 * @param properties A lambda function for configuring the input action parameters.
 *
 * @return A new [InputAction] with the specified properties.
 */
fun inputAction(properties: InputAction.Builder.() -> Unit): InputAction =
    InputAction.builder().apply(properties).build()

/**
 * Builds an NCCO action to enable hosting conference calls, while preserving the communication context.
 *
 * @param name The name of the conversation.
 *
 * @param properties (OPTIONAL) A lambda function for configuring the Conversation action parameters.
 *
 * @return A new [ConversationAction] with the specified properties.
 */
fun conversationAction(name: String, properties: ConversationAction.Builder.() -> Unit = {}): ConversationAction =
    ConversationAction.builder(name).apply(properties).build()

/**
 * Builds an NCCO action to connect the call to another destination. This is the underlying method that other
 * `connectTo*` methods in [Voice] use to build their actions. It is recommended to use those instead.
 *
 * @param endpoint The endpoint to connect the call to.
 *
 * @param properties (OPTIONAL) A lambda function for configuring the connect action parameters.
 *
 * @return A new [ConnectAction] with the specified properties.
 */
fun connectAction(endpoint: ConnectEndpoint,
                  properties: ConnectAction.Builder.() -> Unit = {}): ConnectAction =
    ConnectAction.builder(endpoint).apply(properties).build()

/**
 * Builds an NCCO action to connect the call to a PSTN number.
 *
 * @param number The MSISDN to connect the call to, in E.164 format.
 * @param dtmfAnswer (OPTIONAL) The DTMF digits to send when the call is answered.
 * @param onAnswerUrl (OPTIONAL) The URL to fetch a new NCCO from when the call is answered.
 * @param onAnswerRingback (OPTIONAL) The URL to fetch a ringback tone from when the call is answered.
 * @param properties (OPTIONAL) A lambda function for additional configuration of the connect action parameters.
 *
 * @return A new [ConnectAction] with the [com.vonage.client.voice.ncco.PhoneEndpoint] and specified properties.
 */
fun connectToPstn(number: String, dtmfAnswer: String? = null,
                  onAnswerUrl: String? = null, onAnswerRingback: String? = null,
                  properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectAction(com.vonage.client.voice.ncco.PhoneEndpoint.builder(number)
        .dtmfAnswer(dtmfAnswer).onAnswer(onAnswerUrl, onAnswerRingback).build(), properties
    )

/**
 * Builds an NCCO action to connect the call to a Vonage Business Communications (VBC) extension.
 *
 * @param extension The VBC extension number to connect the call to.
 *
 * @param properties (OPTIONAL) A lambda function for additional configuration of the connect action parameters.
 *
 * @return A new [ConnectAction] with the [com.vonage.client.voice.ncco.VbcEndpoint] and specified properties.
 */
fun connectToVbc(extension: String, properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectAction(com.vonage.client.voice.ncco.VbcEndpoint.builder(extension).build(), properties)

/**
 * Builds an NCCO action to connect the call to an RTC capable application.
 *
 * @param user The username to connect the call to.
 * @param properties (OPTIONAL) A lambda function for additional configuration of the connect action parameters.
 *
 * @return A new [ConnectAction] with the [com.vonage.client.voice.ncco.AppEndpoint] and specified properties.
 */
fun connectToApp(user: String, properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectAction(com.vonage.client.voice.ncco.AppEndpoint.builder(user).build(), properties)

/**
 * Builds an NCCO action to connect the call to a WebSocket endpoint.
 *
 * @param uri The URI of the WebSocket to connect to.
 * @param contentType The internet media type for the audio you are streaming.
 * @param headers (OPTIONAL) A map of custom metadata to send with the WebSocket connection.
 * @param properties (OPTIONAL) A lambda function for additional configuration of the connect action parameters.
 *
 * @return A new [ConnectAction] with the [com.vonage.client.voice.ncco.WebSocketEndpoint] and specified properties.
 */
fun connectToWebsocket(uri: String, contentType: Websocket.ContentType, headers: Map<String, Any>? = null,
                       properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectAction(com.vonage.client.voice.ncco.WebSocketEndpoint.builder(uri, contentType.toString())
        .headers(headers).build(), properties
    )

private fun connectToSip(
    customHeaders: Map<String, Any>?,
    userToUserHeader: String?,
    connectProperties: ConnectAction.Builder.() -> Unit,
    sipProperties: com.vonage.client.voice.ncco.SipEndpoint.Builder.() -> Unit
) : ConnectAction {
    val builder = com.vonage.client.voice.ncco.SipEndpoint.builder().apply(sipProperties)
    if (customHeaders != null) {
        builder.headers(customHeaders)
    }
    if (userToUserHeader != null) {
        builder.userToUserHeader(userToUserHeader)
    }
    return connectAction(builder.build(), connectProperties)
}

/**
 * Builds an NCCO action to connect the call to a SIP endpoint.
 *
 * @param uri The URI of the SIP endpoint to connect to.
 * @param customHeaders (OPTIONAL) A map of custom metadata to send with the SIP connection.
 * @param userToUserHeader (OPTIONAL) A string to send as the User-to-User header, as per RFC 7433.
 * @param properties (OPTIONAL) A lambda function for additional configuration of the connect action parameters.
 *
 * @return A new [ConnectAction] with the [com.vonage.client.voice.ncco.SipEndpoint] and specified properties.
 */
fun connectToSip(uri: String, customHeaders: Map<String, Any>? = null, userToUserHeader: String? = null,
                 properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectToSip(customHeaders, userToUserHeader, properties) { uri(uri) }

/**
 * Builds an NCCO action to connect the call to a SIP endpoint.
 *
 * @param domain Identifier of the SIP domain provisioned using the Programmable SIP API or SIP trunking dashboard.
 * @param user (OPTIONAL) The SIP domain user, which will be used in conjunction with the domain to create the SIP URI.
 * @param customHeaders (OPTIONAL) A map of custom metadata to send with the SIP connection.
 * @param userToUserHeader (OPTIONAL) A string to send as the User-to-User header, as per RFC 7433.
 * @param properties (OPTIONAL) A lambda function for additional configuration of the connect action parameters.
 *
 * @return A new [ConnectAction] with the [com.vonage.client.voice.ncco.SipEndpoint] and specified properties.
 *
 * @since 1.1.4
 */
fun connectToSip(domain: String, user: String?,
                 customHeaders: Map<String, Any>? = null, userToUserHeader: String? = null,
                 properties: ConnectAction.Builder.() -> Unit = {}) : ConnectAction =
    connectToSip(customHeaders, userToUserHeader, properties) { domain(domain).user(user) }

/**
 * Sets the call destination to a Public Switched Telephone Network (PSTN) or mobile number.
 *
 * @param number The MSISDN to call, in E.164 format.
 * @param dtmfAnswer (OPTIONAL) The DTMF digits to send when the call is answered.
 *
 * @return The updated [Call.Builder].
 */
fun Call.Builder.toPstn(number: String, dtmfAnswer: String? = null): Call.Builder =
    to(com.vonage.client.voice.PhoneEndpoint(number, dtmfAnswer))

/**
 * Sets the call destination to a SIP URI.
 *
 * @param uri URI of the SIP endpoint to call.
 * @param customHeaders (OPTIONAL) A map of custom metadata to send with the SIP connection.
 * @param userToUserHeader (OPTIONAL) A string to send as the User-to-User header, as per RFC 7433.
 *
 * @return The updated [Call.Builder].
 */
fun Call.Builder.toSip(uri: String, customHeaders: Map<String, Any>? = null,
                       userToUserHeader: String? = null): Call.Builder =
    to(com.vonage.client.voice.SipEndpoint(uri, customHeaders, userToUserHeader))

/**
 * Sets the call destination to a WebSocket endpoint.
 *
 * @param uri The URI of the WebSocket to connect to.
 * @param contentType The internet media type for the audio you are streaming.
 * @param headers (OPTIONAL) A map of custom metadata to send with the WebSocket connection.
 */
fun Call.Builder.toWebSocket(uri: String, contentType: Websocket.ContentType,
                             headers: Map<String, Any>? = null): Call.Builder =
    to(com.vonage.client.voice.WebSocketEndpoint(uri, contentType.toString(), headers))

/**
 * Sets the call destination to a Vonage Business Communications (VBC) extension.
 *
 * @param extension The VBC extension number to call.
 *
 * @return The updated [Call.Builder].
 */
fun Call.Builder.toVbc(extension: String): Call.Builder =
    to(com.vonage.client.voice.VbcEndpoint(extension))

/**
 * Sets the call destination to an RTC capable application user.
 *
 * @param user The username of the application user to call.
 *
 * @return The updated [Call.Builder].
 */
fun Call.Builder.toApp(user: String): Call.Builder =
    to(com.vonage.client.voice.AppEndpoint(user))
