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

import com.vonage.client.video.*
import java.util.*

/**
 * Implementation of the [Video API](https://developer.vonage.com/en/api/video).
 *
 * Authentication method: JWT.
 */
class Video(private val client: VideoClient) {

    /**
     * Create a new session.
     *
     * @param properties (OPTIONAL) A lambda function to set the parameters of the session.
     *
     * @return The created session metadata.
     */
    fun createSession(properties: CreateSessionRequest.Builder.() -> Unit = {}): CreateSessionResponse =
        client.createSession(CreateSessionRequest.builder().apply(properties).build())

    /**
     * Call this method to work with an existing session.
     *
     * @param sessionId The UUID of the session to work with.
     *
     * @return An [ExistingSession] object with methods to interact with the session.
     */
    fun session(sessionId: String): ExistingSession = ExistingSession(sessionId)

    /**
     * Class for working with an existing session.
     *
     * @property id The session ID.
     */
    inner class ExistingSession internal constructor(id: String): ExistingResource(id) {

        /**
         * Call this method to work with an existing stream.
         *
         * @param streamId UUID of the stream to work with.
         */
        fun stream(streamId: String): ExistingStream = ExistingStream(streamId)

        /**
         * Class for working with an existing stream.
         *
         * @property id The stream ID.
         */
        inner class ExistingStream internal constructor(id: String): ExistingResource(id) {

            /**
             * Retrieves the stream details.
             *
             * @return The stream metadata.
             */
            fun info(): GetStreamResponse = client.getStream(this@ExistingSession.id, id)

            /**
             * Mute the stream.
             */
            fun mute(): Unit = client.muteStream(this@ExistingSession.id, id)

            /**
             * Update the stream's video layout.
             *
             * @param layoutClasses The layout class names to apply to the stream.
             */
            fun setLayout(vararg layoutClasses: String): Unit =
                client.setStreamLayout(this@ExistingSession.id,
                    SessionStream.builder(id).layoutClassList(layoutClasses.toList()).build()
                )
        }

        /**
         * Call this method to work with an existing client (participant).
         *
         * @param connectionId UUID of the connection to work with.
         *
         * @return An [ExistingConnection] object with methods to interact with the connection.
         */
        fun connection(connectionId: String): ExistingConnection = ExistingConnection(connectionId)

        /**
         * Class for working with an existing connection.
         *
         * @property id The connection ID.
         */
        inner class ExistingConnection internal constructor(id: String): ExistingResource(id) {

            /**
             * Force the client to disconnect from the session.
             */
            fun disconnect(): Unit = client.forceDisconnect(this@ExistingSession.id, id)

            /**
             * Send a signal to the client.
             *
             * @param type Type of data that is being sent to the client. This cannot exceed 128 bytes.
             * @param data Payload that is being sent to the client. This cannot exceed 8kb.
             */
            fun signal(type: String, data: String): Unit =
                client.signal(this@ExistingSession.id, id, signalRequest(type, data))

            /**
             * Send DTMF tones to the client. Telephony events are negotiated over SDP and transmitted as
             * RFC4733/RFC2833 digits to the remote endpoint.
             *
             * @param digits The string of DTMF digits to send. This can include 0-9, '*', '#', and 'p'.
             * A 'p' indicates a pause of 500ms (if you need to add a delay in sending the digits).
             */
            fun sendDtmf(digits: String): Unit = client.sendDtmf(this@ExistingSession.id, id, digits)
        }

        /**
         * Mute or unmute all streams in the session, except for those specified.
         *
         * @param active Whether to mute streams in the session (`true`) and enable the mute state of the session,
         * or to disable the mute state of the session (`false`). With the mute state enabled (true), all current
         * and future streams published to the session (except streams in the `excludedStreamIds` array) are muted.
         * When you call this method with the active property set to `false`, future streams published to the
         * session are not muted (but any existing muted streams remain muted).
         *
         * @param excludedStreamIds (OPTIONAL) The stream IDs for streams that should not be muted. If you omit
         * this property, all streams in the session will be muted. This property only applies when the active
         * property is set to true. When the active property is set to false, it is ignored.
         *
         * @return The updated project details.
         */
        fun muteStreams(active: Boolean = true, vararg excludedStreamIds: String): ProjectDetails =
            client.muteSession(id, active,
                if (excludedStreamIds.isNotEmpty()) excludedStreamIds.toList() else null
            )

        /**
         * List all streams in the session. This can be used to get information about layout classes used by a Vonage
         * Video stream. The layout classes define how the stream is displayed in the layout of a broadcast stream.
         *
         * @return A list of streams and their layouts for this session.
         */
        fun listStreams(): List<GetStreamResponse> = client.listStreams(id)

        /**
         * List all Archives of this session.
         *
         * @param count (OPTIONAL) The number of Archives to return (maximum 1000).
         * @param offset (OPTIONAL) Index of the first Archive to return (used for pagination).
         *
         * @return A list of Archives.
         */
        fun listArchives(count: Int = 1000, offset: Int = 0): List<Archive> =
            client.listArchives(listCompositionsFilter(count, offset, id))

        /**
         * List all Broadcasts in the application.
         *
         * @param count (OPTIONAL) The number of Broadcasts to return (maximum 1000).
         * @param offset (OPTIONAL) Index of the first Broadcast to return (used for pagination).
         *
         * @return A list of Broadcasts.
         */
        fun listBroadcasts(count: Int = 1000, offset: Int = 0): List<Broadcast> =
            client.listBroadcasts(listCompositionsFilter(count, offset, id))

        /**
         * Send a signal to all participants (clients) in the session.
         *
         * @param type Type of data that is being sent to the clients. This cannot exceed 128 bytes.
         * @param data Payload that is being sent to the clients. This cannot exceed 8kb.
         */
        fun signalAll(type: String, data: String): Unit =
            client.signalAll(id, signalRequest(type, data))

        /**
         * Send DTMF tones to all participants in the session. Telephony events are negotiated over SDP and transmitted
         * as RFC4733/RFC2833 digits to the remote endpoint.
         *
         * @param digits The string of DTMF digits to send. This can include 0-9, '*', '#', and 'p'.
         * A 'p' indicates a pause of 500ms (if you need to add a delay in sending the digits).
         */
        fun sendDtmf(digits: String): Unit = client.sendDtmf(id, digits)

        /**
         * Start real-time Live Captions for the session.
         *
         * The maximum allowed duration is 4 hours, after which the audio captioning will stop without any effect
         * on the ongoing Vonage Video Session. An event will be posted to your callback URL if provided when
         * starting the captions. Each Vonage Video Session supports only one audio captioning session.
         *
         * @param token A valid Vonage JWT with role set to Moderator.
         * @param properties (OPTIONAL) A lambda function to set the parameters of the audio captioning session.
         *
         * @return Unique ID of the audio captioning session
         */
        fun startCaptions(token: String, properties: CaptionsRequest.Builder.() -> Unit = {}): UUID =
            client.startCaptions(CaptionsRequest.builder()
                .apply(properties).sessionId(id).token(token).build()
            ).captionsId

        /**
         * Stop Live Captions for the session.
         *
         * @param captionsId The unique ID of the audio captioning session.
         */
        fun stopCaptions(captionsId: String): Unit = client.stopCaptions(captionsId)

        /**
         * Establish a SIP connection to this session.
         *
         * The audio from your end of the SIP call is added to the sessionnas an audio-only stream. The Vonage Video
         * Media Router mixes audio from other streams in the session and sends the mixed audio to your SIP endpoint.
         * The call ends when your SIP server sends a `BYE` message to terminate the call. You can also end a call
         * using the [ExistingSession.ExistingConnection.disconnect] method. The Vonage Video SIP gateway automatically
         * ends a call after 5 minutes of inactivity (5 minutes without media received). Also, as a security measure,
         * the Vonage Video SIP gateway closes any SIP call that lasts longer than 6 hours. The SIP interconnect
         * feature requires the session's media mode to be set to [MediaMode.ROUTED]. For more information, including
         * technical details and security considerations, see the Vonage Video SIP interconnect developer guide.
         *
         * @param properties A lambda function to set the parameters of the SIP dial request.
         * You need to provide a token and a SIP URI to establish the connection.
         *
         * @return Details of the SIP connection.
         */
        fun sipDial(properties: SipDialRequest.Builder.() -> Unit): SipDialResponse =
            client.sipDial(SipDialRequest.builder().sessionId(id).apply(properties).build())

        /**
         * Send audio from the session to a WebSocket. This feature is only supported in `routed` sessions.
         *
         * @param properties A lambda function to set the parameters of the WebSocket connection.
         *
         * @return Details of the WebSocket connection.
         */
        fun connectToWebsocket(properties: ConnectRequest.Builder.() -> Unit): ConnectResponse =
            client.connectToWebsocket(ConnectRequest.builder().sessionId(id).apply(properties).build())

        /**
         * Create a new Experience Composer for this session.
         *
         * @param properties A lambda function to set the parameters of the Experience Composer.
         *
         * @return Details of the created Experience Composer.
         */
        fun startRender(properties: RenderRequest.Builder.() -> Unit): RenderResponse =
            client.startRender(RenderRequest.builder().sessionId(id).apply(properties).build())

        /**
         * Create a new Archive from this session.
         *
         * @param properties (OPTIONAL) A lambda function to set the parameters of the Archive.
         *
         * @return Details of the created Archive.
         */
        fun createArchive(properties: Archive.Builder.() -> Unit = {}): Archive =
            client.createArchive(Archive.builder(id).apply(properties).build())

        /**
         * Start a live broadcast for the session to HLS (HTTP Live-Streaming) or RTMP streams.
         *
         * To successfully start broadcasting a session, at least one client must be connected to the session.
         * The live-streaming broadcast can target one HLS endpoint and up to five RTMP servers simultaneously
         * for a session. You can only start live-streaming for sessions that use the Vonage Video Media Router
         * (with the media mode set to [MediaMode.ROUTED]); you cannot use live-streaming with sessions that have
         * the media mode set to [MediaMode.RELAYED].
         *
         * @param properties A lambda function to set the parameters of the broadcast. See [Broadcast.Builder]
         * for details of required and optional parameters.
         *
         * @return Details of the created broadcast.
         */
        fun startBroadcast(properties: Broadcast.Builder.() -> Unit): Broadcast =
            client.createBroadcast(Broadcast.builder(id).apply(properties).build())

        /**
         * Generate a JWT for the session, which can be used in various other methods as required.
         *
         * @param options (OPTIONAL) A lambda function to set the parameters (claims) of the token.
         *
         * @return A new JWT with the specified claims.
         */
        fun generateToken(options: TokenOptions.Builder.() -> Unit = {}): String =
            client.generateToken(id, TokenOptions.builder().apply(options).build())
    }

    /**
     * List all Archives in the application.
     *
     * @param count (OPTIONAL) The number of Archives to return (maximum 1000).
     * @param offset (OPTIONAL) Index of the first Archive to return (used for pagination).
     *
     * @return A list of Archives.
     */
    fun listArchives(count: Int = 1000, offset: Int = 0): List<Archive> =
        client.listArchives(listCompositionsFilter(count, offset))

    /**
     * Call this method to work with an existing archive.
     *
     * @param archiveId UUID of the archive to work with.
     *
     * @return An [ExistingArchive] object with methods to interact with the archive.
     */
    fun archive(archiveId: String): ExistingArchive = ExistingArchive(archiveId)

    /**
     * Class for working with an existing archive.
     *
     * @property id The archive ID.
     */
    inner class ExistingArchive internal constructor(id: String): ExistingResource(id) {

        /**
         * Retrieve archive information.
         *
         * @return Details of the archive.
         */
        fun info(): Archive = client.getArchive(id)

        /**
         * Stop the archive.
         *
         * Archives stop recording after 4 hours (14,400 seconds), or 60 seconds after the last client disconnects
         * from the session, or 60 minutes after the last client stops publishing. However, automatic archives continue
         * recording to multiple consecutive files of up to 4 hours in length each.
         *
         * Calling this method for automatic archives has no effect. Automatic archives continue recording to multiple
         * consecutive files of up to 4 hours (14,400 seconds) in length each, until 60 seconds after the last client
         * disconnects from the session, or 60 minutes after the last client stops publishing a stream to the session.
         *
         * @return Details of the archive.
         */
        fun stop(): Archive = client.stopArchive(id)

        /**
         * Delete the archive.
         */
        fun delete(): Unit = client.deleteArchive(id)

        /**
         * Set the layout for the archive. This only applies if it is composed.
         *
         * @param initialLayout The layout type to use for the archive as an enum. If set to
         * [ScreenLayoutType.CUSTOM], then you must also set the `stylesheet` property.
         *
         * @param screenshareType (OPTIONAL) The layout type to use when there is a screen-sharing stream in the
         * session. Note if you set this property, then `initialLayout` must be set to [ScreenLayoutType.BEST_FIT]
         * and you must leave the `stylesheet` property unset (null).
         *
         * @param stylesheet (OPTIONAL) The CSS stylesheet to use for the archive. If you set this property,
         * then `initialLayout` must be set to [ScreenLayoutType.CUSTOM].
         */
        fun setLayout(initialLayout: ScreenLayoutType,
                      screenshareType: ScreenLayoutType? = null,
                      stylesheet: String? = null): Unit =
            client.updateArchiveLayout(id,
                StreamCompositionLayout.builder(initialLayout)
                    .screenshareType(screenshareType)
                    .stylesheet(stylesheet)
                    .build()
            )

        /**
         * Add a stream to the archive. This only applies if it's a composed archive that was started with
         * the `streamMode` set to [StreamMode.MANUAL].
         *
         * @param streamId UUID of the stream to add.
         * @param audio (OPTIONAL) Whether the composed archive should include the stream's audio.
         * @param video (OPTIONAL) Whether the composed archive should include the stream's video.
         */
        fun addStream(streamId: String, audio: Boolean = true, video: Boolean = true): Unit =
            client.addArchiveStream(id, streamId, audio, video)

        /**
         * Remove a stream from the archive. This only applies if it's a composed archive that was started with
         * the `streamMode` set to [StreamMode.MANUAL].
         *
         * @param streamId UUID of the stream to remove.
         */
        fun removeStream(streamId: String): Unit = client.removeArchiveStream(id, streamId)
    }

    /**
     * List all Broadcasts in the application.
     *
     * @param count (OPTIONAL) The number of Broadcasts to return (maximum 1000).
     * @param offset (OPTIONAL) Index of the first Broadcast to return (used for pagination).
     *
     * @return A list of Broadcasts.
     */
    fun listBroadcasts(count: Int = 1000, offset: Int = 0): List<Broadcast> =
        client.listBroadcasts(listCompositionsFilter(count, offset))

    /**
     * Call this method to work with an existing Broadcast.
     *
     * @param broadcastId UUID of the Broadcast to work with.
     *
     * @return An [ExistingBroadcast] object with methods to interact with the Broadcast.
     */
    fun broadcast(broadcastId: String): ExistingBroadcast = ExistingBroadcast(broadcastId)

    /**
     * Class for working with an existing broadcast.
     *
     * @property id The broadcast ID.
     */
    inner class ExistingBroadcast internal constructor(id: String): ExistingResource(id) {

        /**
         * Retrieves the broadcast details.
         *
         * @return The broadcast metadata.
         */
        fun info(): Broadcast = client.getBroadcast(id)

        /**
         * Stops the broadcast.
         *
         * @return Details of the broadcast.
         */
        fun stop(): Broadcast = client.stopBroadcast(id)

        /**
         * Set the layout for the broadcast.
         *
         * @param initialLayout The layout type to use for the broadcast as an enum. If set to
         * [ScreenLayoutType.CUSTOM], then you must also set the `stylesheet` property.
         *
         * @param screenshareType (OPTIONAL) The layout type to use when there is a screen-sharing stream in the
         * session. Note if you set this property, then `initialLayout` must be set to [ScreenLayoutType.BEST_FIT]
         * and you must leave the `stylesheet` property unset (null).
         *
         * @param stylesheet (OPTIONAL) The CSS stylesheet to use for the broadcast. If you set this property,
         * then `initialLayout` must be set to [ScreenLayoutType.CUSTOM].
         */
        fun setLayout(initialLayout: ScreenLayoutType,
                      screenshareType: ScreenLayoutType? = null,
                      stylesheet: String? = null): Unit =
            client.updateBroadcastLayout(id,
                StreamCompositionLayout.builder(initialLayout)
                    .screenshareType(screenshareType)
                    .stylesheet(stylesheet)
                    .build()
            )

        /**
         * Add a stream to the broadcast.
         *
         * @param streamId UUID of the stream to add.
         * @param audio (OPTIONAL) Whether the broadcast should include the stream's audio.
         * @param video (OPTIONAL) Whether the broadcast should include the stream's video.
         */
        fun addStream(streamId: String, audio: Boolean = true, video: Boolean = true): Unit =
            client.addBroadcastStream(id, streamId, audio, video)

        /**
         * Remove a stream from the broadcast.
         *
         * @param streamId UUID of the stream to remove.
         */
        fun removeStream(streamId: String): Unit = client.removeBroadcastStream(id, streamId)
    }

    /**
     * List all Experience Composers in the application.
     *
     * @param count (OPTIONAL) The number of Experience Composers to return (maximum 1000).
     * @param offset (OPTIONAL) Index of the first Experience Composer to return (used for pagination).
     *
     * @return A list of Experience Composers.
     */
    fun listRenders(count: Int = 1000, offset: Int = 0): List<RenderResponse> =
        client.listRenders(ListStreamCompositionsRequest.builder().count(count).offset(offset).build())

    /**
     * Call this method to work with an existing Experience Composer.
     *
     * @param renderId UUID of the Experience Composer to work with.
     *
     * @return An [ExistingRender] object with methods to interact with the Experience Composer.
     */
    fun render(renderId: String): ExistingRender = ExistingRender(renderId)

    /**
     * Class for working with an existing Experience Composer.
     *
     * @property id The render ID.
     */
    inner class ExistingRender internal constructor(id: String): ExistingResource(id) {

        /**
         * Retrieves the Experience Composer.
         *
         * @return Details of the Experience Composer.
         */
        fun info(): RenderResponse = client.getRender(id)

        /**
         * Stops the Experience Composer.
         */
        fun stop(): Unit = client.stopRender(id)
    }
}

private fun listCompositionsFilter(count: Int, offset: Int, sessionId: String? = null):
        ListStreamCompositionsRequest = ListStreamCompositionsRequest.builder()
            .count(count).offset(offset).sessionId(sessionId).build()

private fun signalRequest(type: String, data: String): SignalRequest =
    SignalRequest.builder().type(type).data(data).build()

private fun streamCompositionLayout(initialLayout: ScreenLayoutType,
                                    screenshareType: ScreenLayoutType?,
                                    stylesheet: String?): StreamCompositionLayout =
    StreamCompositionLayout.builder(initialLayout)
        .screenshareType(screenshareType).stylesheet(stylesheet).build()

/**
 * Adds an RTMP stream to the broadcast builder.
 *
 * @param properties A lambda function to set the parameters of the RTMP stream.
 *
 * @return The updated broadcast builder.
 */
fun Broadcast.Builder.addRtmpStream(properties: Rtmp.Builder.() -> Unit): Broadcast.Builder =
    addRtmpStream(Rtmp.builder().apply(properties).build())

/**
 * Adds an HTTP Live Stream to the broadcast builder.
 *
 * @param properties (OPTIONAL) A lambda function to set the parameters of the HLS stream.
 *
 * @return The updated broadcast builder.
 */
fun Broadcast.Builder.hls(properties: Hls.Builder.() -> Unit = {}): Broadcast.Builder =
    hls(Hls.builder().apply(properties).build())

/**
 * Sets the layout for a composed archive. If this option is specified,
 * [Archive.Builder.outputMode] must be set to [OutputMode.COMPOSED].
 *
 * @param initialLayout The layout type to use for the archive as an enum. If set to
 * [ScreenLayoutType.CUSTOM], then you must also set the `stylesheet` property.
 *
 * @param screenshareType (OPTIONAL) The layout type to use when there is a screen-sharing stream in the
 * session. Note if you set this property, then `initialLayout` must be set to [ScreenLayoutType.BEST_FIT]
 * and you must leave the `stylesheet` property unset (null).
 *
 * @param stylesheet (OPTIONAL) The CSS stylesheet to use for the archive. If you set this property,
 * then `initialLayout` must be set to [ScreenLayoutType.CUSTOM].
 *
 * @return The updated archive builder.
 */
fun Archive.Builder.layout(initialLayout: ScreenLayoutType,
                           screenshareType: ScreenLayoutType? = null,
                           stylesheet: String? = null): Archive.Builder =
    layout(streamCompositionLayout(initialLayout, screenshareType, stylesheet))

/**
 * Specify this to assign the initial layout type for the broadcast. If you do not specify an initial layout type,
 * the broadcast stream uses [ScreenLayoutType.BEST_FIT] as the default layout type.
 *
 * @param initialLayout The layout type to use for the broadcast as an enum. If set to
 * [ScreenLayoutType.CUSTOM], then you must also set the `stylesheet` property.
 *
 * @param screenshareType (OPTIONAL) The layout type to use when there is a screen-sharing stream in the
 * session. Note if you set this property, then `initialLayout` must be set to [ScreenLayoutType.BEST_FIT]
 * and you must leave the `stylesheet` property unset (null).
 *
 * @param stylesheet (OPTIONAL) The CSS stylesheet to use for the broadcast. If you set this property,
 * then `initialLayout` must be set to [ScreenLayoutType.CUSTOM].
 *
 * @return The updated broadcast builder.
 */
fun Broadcast.Builder.layout(initialLayout: ScreenLayoutType = ScreenLayoutType.BEST_FIT,
                           screenshareType: ScreenLayoutType? = null,
                           stylesheet: String? = null): Broadcast.Builder =
    layout(streamCompositionLayout(initialLayout, screenshareType, stylesheet))
