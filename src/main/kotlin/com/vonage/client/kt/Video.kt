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

    inner class ExistingSession internal constructor(val sessionId: String) {

        fun stream(streamId: String): ExistingStream = ExistingStream(streamId)

        inner class ExistingStream internal constructor(val streamId: String) {

            fun info(): GetStreamResponse = client.getStream(sessionId, streamId)

            fun mute(): Unit = client.muteStream(sessionId, streamId)

            fun setLayout(vararg layoutClasses: String): Unit =
                client.setStreamLayout(sessionId,
                    SessionStream.builder(streamId).layoutClassList(layoutClasses.toList()).build()
                )
        }

        fun connection(connectionId: String): ExistingConnection = ExistingConnection(connectionId)

        inner class ExistingConnection internal constructor(val connectionId: String) {

            fun disconnect(): Unit = client.forceDisconnect(sessionId, connectionId)

            fun signal(type: String, data: String): Unit =
                client.signal(sessionId, connectionId, signalRequest(type, data))

            fun sendDtmf(digits: String): Unit = client.sendDtmf(sessionId, connectionId, digits)
        }

        fun muteStreams(active: Boolean = true, vararg excludedStreamIds: String): ProjectDetails =
            client.muteSession(sessionId, active,
                if (excludedStreamIds.isNotEmpty()) excludedStreamIds.toList() else null
            )

        fun listStreams(): List<GetStreamResponse> = client.listStreams(sessionId)

        fun listArchives(count: Int = 1000, offset: Int = 0): List<Archive> =
            client.listArchives(listCompositionsFilter(count, offset, sessionId))

        fun listBroadcasts(count: Int = 1000, offset: Int = 0): List<Broadcast> =
            client.listBroadcasts(listCompositionsFilter(count, offset, sessionId))

        fun signalAll(type: String, data: String): Unit =
            client.signalAll(sessionId, signalRequest(type, data))

        fun sendDtmf(digits: String): Unit = client.sendDtmf(sessionId, digits)

        fun startCaptions(token: String, properties: CaptionsRequest.Builder.() -> Unit): UUID =
            client.startCaptions(CaptionsRequest.builder()
                .apply(properties).sessionId(sessionId).token(token).build()
            ).captionsId

        fun stopCaptions(captionsId: String): Unit = client.stopCaptions(captionsId)

        fun createArchive(properties: Archive.Builder.() -> Unit): Archive =
            client.createArchive(Archive.builder(sessionId).apply(properties).build())

        fun startBroadcast(properties: Broadcast.Builder.() -> Unit): Broadcast =
            client.createBroadcast(Broadcast.builder(sessionId).apply(properties).build())

        fun generateToken(options: TokenOptions.Builder.() -> Unit = {}): String =
            client.generateToken(sessionId, TokenOptions.builder().apply(options).build())
    }

    fun sipDial(properties: SipDialRequest.Builder.() -> Unit): SipDialResponse =
        client.sipDial(SipDialRequest.builder().apply(properties).build())

    fun connectToWebsocket(properties: ConnectRequest.Builder.() -> Unit): ConnectResponse =
        client.connectToWebsocket(ConnectRequest.builder().apply(properties).build())

    fun listArchives(count: Int = 1000, offset: Int = 0): List<Archive> =
        client.listArchives(listCompositionsFilter(count, offset))

    fun archive(archiveId: String): ExistingArchive = ExistingArchive(archiveId)

    inner class ExistingArchive internal constructor(val archiveId: String) {

        fun info(): Archive = client.getArchive(archiveId)

        fun stop(): Archive = client.stopArchive(archiveId)

        fun delete(): Unit = client.deleteArchive(archiveId)

        fun setLayout(initialLayout: ScreenLayoutType,
                      screenshareType: ScreenLayoutType? = null,
                      stylesheet: String? = null): Unit =
            client.updateArchiveLayout(archiveId,
                StreamCompositionLayout.builder(initialLayout)
                    .screenshareType(screenshareType)
                    .stylesheet(stylesheet)
                    .build()
            )

        fun addStream(streamId: String, audio: Boolean = true, video: Boolean = true) =
            client.addArchiveStream(archiveId, streamId, audio, video)

        fun removeStream(streamId: String): Unit = client.removeArchiveStream(archiveId, streamId)
    }

    fun listBroadcasts(count: Int = 1000, offset: Int = 0): List<Broadcast> =
        client.listBroadcasts(listCompositionsFilter(count, offset))

    fun broadcast(broadcastId: String): ExistingBroadcast = ExistingBroadcast(broadcastId)

    inner class ExistingBroadcast internal constructor(val broadcastId: String) {

        fun info(): Broadcast = client.getBroadcast(broadcastId)

        fun stop(): Broadcast = client.stopBroadcast(broadcastId)

        fun setLayout(initialLayout: ScreenLayoutType,
                      screenshareType: ScreenLayoutType? = null,
                      stylesheet: String? = null): Unit =
            client.updateBroadcastLayout(broadcastId,
                StreamCompositionLayout.builder(initialLayout)
                    .screenshareType(screenshareType)
                    .stylesheet(stylesheet)
                    .build()
            )

        fun addStream(streamId: String, audio: Boolean = true, video: Boolean = true) =
            client.addBroadcastStream(broadcastId, streamId, audio, video)

        fun removeStream(streamId: String): Unit = client.removeBroadcastStream(broadcastId, streamId)
    }

    fun startRender(properties: RenderRequest.Builder.() -> Unit): RenderResponse =
        client.startRender(RenderRequest.builder().apply(properties).build())

    fun listRenders(count: Int = 1000, offset: Int = 0): List<RenderResponse> =
        client.listRenders(ListStreamCompositionsRequest.builder().count(count).offset(offset).build())

    fun render(renderId: String): ExistingRender = ExistingRender(renderId)

    inner class ExistingRender internal constructor(val renderId: String) {

        fun info(): RenderResponse = client.getRender(renderId)

        fun stop(): Unit = client.stopRender(renderId)
    }
}

private fun listCompositionsFilter(count: Int, offset: Int, sessionId: String? = null):
        ListStreamCompositionsRequest = ListStreamCompositionsRequest.builder()
            .count(count).offset(offset).sessionId(sessionId).build()

private fun signalRequest(type: String, data: String): SignalRequest =
    SignalRequest.builder().type(type).data(data).build()

fun rtmp(properties: Rtmp.Builder.() -> Unit): Rtmp = Rtmp.builder().apply(properties).build()

fun Broadcast.Builder.addRtmpStream(properties: Rtmp.Builder.() -> Unit): Broadcast.Builder =
    addRtmpStream(rtmp(properties))

fun Broadcast.Builder.hls(hls: Hls.Builder.() -> Unit = {}): Broadcast.Builder =
    hls(Hls.builder().apply(hls).build())
