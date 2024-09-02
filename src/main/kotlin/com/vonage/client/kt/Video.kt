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

class Video(private val client: VideoClient) {

    fun createSession(properties: CreateSessionRequest.Builder.() -> Unit = {}): CreateSessionResponse =
        client.createSession(CreateSessionRequest.builder().apply(properties).build())

    fun session(sessionId: String): ExistingSession = ExistingSession(sessionId)

    inner class ExistingSession internal constructor(id: String): ExistingResource(id) {

        fun stream(streamId: String): ExistingStream = ExistingStream(streamId)

        inner class ExistingStream internal constructor(id: String): ExistingResource(id) {

            fun info(): GetStreamResponse = client.getStream(this@ExistingSession.id, id)

            fun mute(): Unit = client.muteStream(this@ExistingSession.id, id)

            fun setLayout(vararg layoutClasses: String): Unit =
                client.setStreamLayout(this@ExistingSession.id,
                    SessionStream.builder(id).layoutClassList(layoutClasses.toList()).build()
                )
        }

        fun connection(connectionId: String): ExistingConnection = ExistingConnection(connectionId)

        inner class ExistingConnection internal constructor(id: String): ExistingResource(id) {

            fun disconnect(): Unit = client.forceDisconnect(this@ExistingSession.id, id)

            fun signal(type: String, data: String): Unit =
                client.signal(this@ExistingSession.id, id, signalRequest(type, data))

            fun sendDtmf(digits: String): Unit = client.sendDtmf(this@ExistingSession.id, id, digits)
        }

        fun muteStreams(active: Boolean = true, vararg excludedStreamIds: String): ProjectDetails =
            client.muteSession(id, active,
                if (excludedStreamIds.isNotEmpty()) excludedStreamIds.toList() else null
            )

        fun listStreams(): List<GetStreamResponse> = client.listStreams(id)

        fun listArchives(count: Int = 1000, offset: Int = 0): List<Archive> =
            client.listArchives(listCompositionsFilter(count, offset, id))

        fun listBroadcasts(count: Int = 1000, offset: Int = 0): List<Broadcast> =
            client.listBroadcasts(listCompositionsFilter(count, offset, id))

        fun signalAll(type: String, data: String): Unit =
            client.signalAll(id, signalRequest(type, data))

        fun sendDtmf(digits: String): Unit = client.sendDtmf(id, digits)

        fun startCaptions(token: String, properties: CaptionsRequest.Builder.() -> Unit = {}): UUID =
            client.startCaptions(CaptionsRequest.builder()
                .apply(properties).sessionId(id).token(token).build()
            ).captionsId

        fun stopCaptions(captionsId: String): Unit = client.stopCaptions(captionsId)

        fun createArchive(properties: Archive.Builder.() -> Unit = {}): Archive =
            client.createArchive(Archive.builder(id).apply(properties).build())

        fun startBroadcast(properties: Broadcast.Builder.() -> Unit): Broadcast =
            client.createBroadcast(Broadcast.builder(id).apply(properties).build())

        fun generateToken(options: TokenOptions.Builder.() -> Unit = {}): String =
            client.generateToken(id, TokenOptions.builder().apply(options).build())
    }

    fun sipDial(properties: SipDialRequest.Builder.() -> Unit): SipDialResponse =
        client.sipDial(SipDialRequest.builder().apply(properties).build())

    fun connectToWebsocket(properties: ConnectRequest.Builder.() -> Unit): ConnectResponse =
        client.connectToWebsocket(ConnectRequest.builder().apply(properties).build())

    fun listArchives(count: Int = 1000, offset: Int = 0): List<Archive> =
        client.listArchives(listCompositionsFilter(count, offset))

    fun archive(archiveId: String): ExistingArchive = ExistingArchive(archiveId)

    inner class ExistingArchive internal constructor(id: String): ExistingResource(id) {

        fun info(): Archive = client.getArchive(id)

        fun stop(): Archive = client.stopArchive(id)

        fun delete(): Unit = client.deleteArchive(id)

        fun setLayout(initialLayout: ScreenLayoutType,
                      screenshareType: ScreenLayoutType? = null,
                      stylesheet: String? = null): Unit =
            client.updateArchiveLayout(id,
                StreamCompositionLayout.builder(initialLayout)
                    .screenshareType(screenshareType)
                    .stylesheet(stylesheet)
                    .build()
            )

        fun addStream(streamId: String, audio: Boolean = true, video: Boolean = true) =
            client.addArchiveStream(id, streamId, audio, video)

        fun removeStream(streamId: String): Unit = client.removeArchiveStream(id, streamId)
    }

    fun listBroadcasts(count: Int = 1000, offset: Int = 0): List<Broadcast> =
        client.listBroadcasts(listCompositionsFilter(count, offset))

    fun broadcast(broadcastId: String): ExistingBroadcast = ExistingBroadcast(broadcastId)

    inner class ExistingBroadcast internal constructor(id: String): ExistingResource(id) {

        fun info(): Broadcast = client.getBroadcast(id)

        fun stop(): Broadcast = client.stopBroadcast(id)

        fun setLayout(initialLayout: ScreenLayoutType,
                      screenshareType: ScreenLayoutType? = null,
                      stylesheet: String? = null): Unit =
            client.updateBroadcastLayout(id,
                StreamCompositionLayout.builder(initialLayout)
                    .screenshareType(screenshareType)
                    .stylesheet(stylesheet)
                    .build()
            )

        fun addStream(streamId: String, audio: Boolean = true, video: Boolean = true) =
            client.addBroadcastStream(id, streamId, audio, video)

        fun removeStream(streamId: String): Unit = client.removeBroadcastStream(id, streamId)
    }

    fun startRender(properties: RenderRequest.Builder.() -> Unit): RenderResponse =
        client.startRender(RenderRequest.builder().apply(properties).build())

    fun listRenders(count: Int = 1000, offset: Int = 0): List<RenderResponse> =
        client.listRenders(ListStreamCompositionsRequest.builder().count(count).offset(offset).build())

    fun render(renderId: String): ExistingRender = ExistingRender(renderId)

    inner class ExistingRender internal constructor(id: String): ExistingResource(id) {

        fun info(): RenderResponse = client.getRender(id)

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

fun Broadcast.Builder.addRtmpStream(properties: Rtmp.Builder.() -> Unit): Broadcast.Builder =
    addRtmpStream(Rtmp.builder().apply(properties).build())

fun Broadcast.Builder.hls(hls: Hls.Builder.() -> Unit = {}): Broadcast.Builder =
    hls(Hls.builder().apply(hls).build())

fun Archive.Builder.layout(initialLayout: ScreenLayoutType,
                           screenshareType: ScreenLayoutType? = null,
                           stylesheet: String? = null): Archive.Builder =
    layout(streamCompositionLayout(initialLayout, screenshareType, stylesheet))

fun Broadcast.Builder.layout(initialLayout: ScreenLayoutType,
                           screenshareType: ScreenLayoutType? = null,
                           stylesheet: String? = null): Broadcast.Builder =
    layout(streamCompositionLayout(initialLayout, screenshareType, stylesheet))
