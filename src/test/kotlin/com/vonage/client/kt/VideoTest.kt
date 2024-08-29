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

import com.auth0.jwt.JWT
import com.vonage.client.video.*
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.*

class VideoTest : AbstractTest() {
    private val client = vonage.video
    private val authType = AuthType.JWT
    private val baseUrl = "/v2/project/$applicationId"
    private val sessionId = "flR1ZSBPY3QgMjkgMTI6MTM6MjMgUERUIDIwMTN"
    private val connectionId = "e9f8c166-6c67-440d-994a-04fb6dfed007"
    private val streamId = "8b732909-0a06-46a2-8ea8-074e64d43422"
    private val archiveId = "b40ef09b-3811-4726-b508-e41a0f96c68f"
    private val broadcastId = "93e36bb9-b72c-45b6-a9ea-5c37dbc49906"
    private val captionsId = "7c0680fc-6274-4de5-a66f-d0648e8d3ac2"
    private val audioConnectorId = "b0a5a8c7-dc38-459f-a48d-a7f2008da853"
    private val renderId = "1248e707-0b81-464c-9789-f46ad10e7764"
    private val sipCallId = "b0a5a8c7-dc38-459f-a48d-a7f2008da853"
    private val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2OTkwNDMxMTEsImV4cCI6MTY5OTA2NDcxMSwianRpIjoiMW1pODlqRk9meVpRIiwiYXBwbGljYXRpb25faWQiOiIxMjMxMjMxMi0zODExLTQ3MjYtYjUwOC1lNDFhMGY5NmM2OGYiLCJzdWIiOiJ2aWRlbyIsImFjbCI6IiJ9.o3U506EejsS8D5Tob90FG1NC1cR69fh3pFOpxnyTHVFfgqI6NWuuN8lEwrS3Zb8bGxE_A9LyyUZ2y4uqLpyXRw"
    private val createdAtLong = 1414642898000L
    private val updatedAtLong = 1437676551029L
    private val createdAtInstant = Instant.ofEpochMilli(createdAtLong)
    private val updatedAtInstant = Instant.ofEpochMilli(updatedAtLong)
    private val sessionUrl = "$baseUrl/session/$sessionId"
    private val connectionBaseUrl = "$sessionUrl/connection/$connectionId"
    private val streamBaseUrl = "$sessionUrl/stream"
    private val streamUrl = "$streamBaseUrl/$streamId"
    private val archiveBaseUrl = "$baseUrl/archive/$archiveId"
    private val broadcastBaseUrl = "$baseUrl/broadcast/$broadcastId"
    private val broadcastLayoutUrl = "$broadcastBaseUrl/layout"
    private val broadcastStreamsUrl = "$broadcastBaseUrl/streams"
    private val archiveLayoutUrl = "$archiveBaseUrl/layout"
    private val archiveStreamsUrl = "$archiveBaseUrl/streams"
    private val renderBaseUrl = "$baseUrl/render"
    private val renderUrl = "$renderBaseUrl/$renderId"
    private val existingSession = client.session(sessionId)
    private val existingConnection = existingSession.connection(connectionId)
    private val existingStream = existingSession.stream(streamId)
    private val existingArchive = client.archive(archiveId)
    private val existingBroadcast = client.broadcast(broadcastId)
    private val existingRender = client.render(renderId)
    private val type = "chat"
    private val data = "Text of the chat message"
    private val signalRequestMap = mapOf("type" to type, "data" to data)
    private val headers = mapOf("k1" to "Value 1", "Key 2" to "2 Val")
    private val mediaUrl = "$exampleUrlBase/media"
    private val maxDuration = 1800
    private val maxBitrate = 2000000
    private val archiveDuration = 5049L
    private val archiveSize = 247748791L
    private val videoType = VideoType.CAMERA
    private val streamName = "My Stream"
    private val archiveName = "Test Archive"
    private val renderName = "Composed stream for Live event #1337"
    private val multiBroadcastTag = "broadcast_tag_provided"
    private val multiArchiveTag = "my-multi-archive"
    private val offset = 16
    private val count = 450
    private val customOffsetCountMap = mapOf("offset" to offset, "count" to count)
    private val defaultOffsetCountMap = mapOf("offset" to 0, "count" to 1000)
    private val layoutClasses = listOf("full", "no-border")
    private val streamLayoutMap = mapOf(
        "id" to streamId,
        "videoType" to videoType.name.lowercase(),
        "name" to streamName,
        "layoutClassList" to layoutClasses
    )
    private val renderResponseMap = mapOf(
        "id" to renderId,
        "sessionId" to sessionId,
        "applicationId" to applicationId,
        "createdAt" to createdAtLong,
        "callbackUrl" to statusCallbackUrl,
        "updatedAt" to updatedAtLong,
        "name" to renderName,
        "url" to mediaUrl,
        "resolution" to "480x640",
        "status" to "starting",
        "streamId" to streamId
    )
    private val stylesheet = "stream.instructor {position: relative; width: 90%;  height:45%;}"
    private val rtmpId = randomUuidStr
    private val rtmpServerUrl = "https://rtmp.example.org/server"
    private val rtmpStatus = RtmpStatus.CONNECTING
    private val archiveStatus = ArchiveStatus.UPLOADED
    private val broadcastStatus = BroadcastStatus.STARTED
    private val broadcastResolutionStr = "640x480"
    private val broadcastResolution = Resolution.SD_LANDSCAPE
    private val archiveResolutionStr = "1920x1080"
    private val archiveResoltion = Resolution.FHD_LANDSCAPE
    private val rtmpMap = mapOf(
        "id" to rtmpId,
        "serverUrl" to rtmpServerUrl,
        "status" to rtmpStatus.name.lowercase(),
        "streamName" to streamName
    )
    private val dvr = false
    private val lowLatency = true
    private val hlsUrl = "https://hls.example.com/stream.m3u8"
    private val broadcastAudio = true
    private val broadcastVideo = false
    private val archiveHasAudio = false
    private val archiveHasVideo = true
    private val broadcastStreamMode = StreamMode.MANUAL
    private val archiveStreamMode = StreamMode.AUTO
    private val streamIdOnly = mapOf("streamId" to streamId)
    private val streamAudioAndVideo = streamIdOnly + mapOf("hasAudio" to true, "hasVideo" to true)
    private val streamAudioNoVideo = mapOf("streamId" to randomUuidStr, "hasAudio" to true, "hasVideo" to false)
    private val streamVideoNoAudio = mapOf("streamId" to randomUuidStr, "hasAudio" to false, "hasVideo" to true)
    private val streamsList = listOf(streamIdOnly, streamAudioAndVideo, streamAudioNoVideo, streamVideoNoAudio)
    private val removeStreamMap = mapOf("removeStream" to streamId)
    private val pipLayoutMap = mapOf(
        "type" to "bestFit",
        "screenshareType" to "pip"
    )
    private val customLayoutMap = mapOf(
        "type" to "custom",
        "stylesheet" to stylesheet
    )
    private val archiveVideoUrl = "https://tokbox.com.archive2.s3.amazonaws.com/123456/$archiveId/archive.mp4"
    private val broadcastResponseMap = mapOf(
        "id" to broadcastId,
        "sessionId" to sessionId,
        "applicationId" to applicationId,
        "multiBroadcastTag" to multiBroadcastTag,
        "createdAt" to createdAtLong,
        "updatedAt" to updatedAtLong,
        "maxDuration" to maxDuration,
        "maxBitrate" to maxBitrate,
        "broadcastUrls" to mapOf(
            "hls" to hlsUrl,
            "rtmp" to listOf(rtmpMap, emptyMap())
        ),
        "settings" to mapOf(
            "hls" to mapOf(
                "dvr" to dvr,
                "lowLatency" to lowLatency
            )
        ),
        "resolution" to broadcastResolutionStr,
        "hasAudio" to broadcastAudio,
        "hasVideo" to broadcastVideo,
        "streamMode" to broadcastStreamMode.name.lowercase(),
        "status" to broadcastStatus.name.lowercase(),
        "streams" to streamsList
    )
    private val archiveResponseMap = mapOf(
        "id" to archiveId,
        "sessionId" to sessionId,
        "applicationId" to applicationId,
        "multiArchiveTag" to multiArchiveTag,
        "name" to archiveName,
        "createdAt" to createdAtLong,
        "duration" to archiveDuration,
        "size" to archiveSize,
        "status" to archiveStatus.name.lowercase(),
        "streamMode" to archiveStreamMode.name.lowercase(),
        "resolution" to archiveResolutionStr,
        "url" to archiveVideoUrl,
        "hasAudio" to archiveHasAudio,
        "hasVideo" to archiveHasVideo,
        "streams" to streamsList
    )

    private fun addStreamMap(audio: Boolean = true, video: Boolean = true): Map<String, Any> =
        mapOf("addStream" to streamId, "hasAudio" to audio, "hasVideo" to video)

    private fun assertEqualsSampleStream(response: GetStreamResponse) {
        assertNotNull(response)
        assertEquals(UUID.fromString(streamId), response.id)
        assertEquals(videoType, response.videoType)
        assertEquals(streamName, response.name)
        assertEquals(layoutClasses, response.layoutClassList)
    }

    private fun assertEqualsSampleRender(response: RenderResponse) {
        assertNotNull(response)
        assertEquals(UUID.fromString(renderId), response.id)
        assertEquals(sessionId, response.sessionId)
        assertEquals(UUID.fromString(applicationId), response.applicationId)
        assertEquals(createdAtLong, response.createdAt)
        assertEquals(URI.create(statusCallbackUrl), response.callbackUrl)
        assertEquals(updatedAtLong, response.updatedAt)
        assertEquals(URI.create(mediaUrl), response.url)
        assertEquals(Resolution.SD_PORTRAIT, response.resolution)
        assertEquals(RenderStatus.STARTING, response.status)
        assertEquals(UUID.fromString(streamId), response.streamId)
    }
    
    private fun assertEqualsVideoStreams(streams: List<VideoStream>) {
        assertNotNull(streams)
        assertEquals(4, streams.size)
        val idOnly = streams[0]
        assertNotNull(idOnly)
        assertEquals(UUID.fromString(streamId), idOnly.streamId)
        assertNull(idOnly.hasAudio())
        assertNull(idOnly.hasVideo())
        val audioAndVideo = streams[1]
        assertNotNull(audioAndVideo)
        assertEquals(UUID.fromString(streamId), audioAndVideo.streamId)
        assertTrue(audioAndVideo.hasAudio())
        assertTrue(audioAndVideo.hasVideo())
        val audioNoVideo = streams[2]
        assertNotNull(audioNoVideo)
        assertEquals(randomUuid, audioNoVideo.streamId)
        assertTrue(audioNoVideo.hasAudio())
        assertFalse(audioNoVideo.hasVideo())
        val videoNoAudio = streams[3]
        assertNotNull(videoNoAudio)
        assertEquals(randomUuid, videoNoAudio.streamId)
        assertFalse(videoNoAudio.hasAudio())
        assertTrue(videoNoAudio.hasVideo())
    }

    private fun assertEqualsSampleArchive(response: Archive) {
        assertNotNull(response)
        assertEquals(UUID.fromString(archiveId), response.id)
        assertEquals(sessionId, response.sessionId)
        assertEquals(UUID.fromString(applicationId), response.applicationId)
        assertEquals(multiArchiveTag, response.multiArchiveTag)
        assertEquals(archiveName, response.name)
        assertEquals(createdAtInstant, response.createdAt)
        assertEquals(Duration.ofSeconds(archiveDuration), response.duration)
        assertEquals(archiveSize, response.size)
        assertEquals(archiveStatus, response.status)
        assertEquals(archiveStreamMode, response.streamMode)
        assertEquals(archiveResoltion, response.resolution)
        assertEquals(URI.create(archiveVideoUrl), response.url)
        assertTrue(response.hasVideo())
        assertFalse(response.hasAudio())
        assertEqualsVideoStreams(response.streams)
    }

    private fun assertEqualsSampleBroadcast(response: Broadcast) {
        assertNotNull(response)
        assertEquals(UUID.fromString(broadcastId), response.id)
        assertEquals(sessionId, response.sessionId)
        assertEquals(UUID.fromString(applicationId), response.applicationId)
        assertEquals(multiBroadcastTag, response.multiBroadcastTag)
        assertEquals(createdAtInstant, response.createdAt)
        assertEquals(updatedAtInstant, response.updatedAt)
        assertEquals(Duration.ofSeconds(maxDuration.toLong()), response.maxDuration)
        assertEquals(maxBitrate, response.maxBitrate)
        val broadcastUrls = response.broadcastUrls
        assertNotNull(broadcastUrls)
        assertEquals(URI.create(hlsUrl), broadcastUrls.hls)
        val rtmps = broadcastUrls.rtmps
        assertNotNull(rtmps)
        assertEquals(2, rtmps.size)
        val mainRtmp = rtmps[0]
        assertNotNull(mainRtmp)
        assertEquals(rtmpId, mainRtmp.id)
        assertEquals(URI.create(rtmpServerUrl), mainRtmp.serverUrl)
        assertEquals(rtmpStatus, mainRtmp.status)
        assertEquals(streamName, mainRtmp.streamName)
        val emptyRtmp = rtmps[1]
        assertNotNull(emptyRtmp)
        assertNull(emptyRtmp.id)
        assertNull(emptyRtmp.serverUrl)
        assertNull(emptyRtmp.status)
        assertNull(emptyRtmp.streamName)
        val hls = response.hlsSettings
        assertNotNull(hls)
        assertEquals(dvr, hls.dvr())
        assertEquals(lowLatency, hls.lowLatency())
        assertEquals(broadcastResolution, response.resolution)
        assertEquals(broadcastAudio, response.hasAudio())
        assertEquals(broadcastVideo, response.hasVideo())
        assertEquals(broadcastStreamMode, response.streamMode)
        assertEquals(broadcastStatus, response.status)
        assertEqualsVideoStreams(response.streams)
    }

    private fun assertEqualsJwt(encoded: String, role: Role = Role.PUBLISHER,
                                ttl: Duration = Duration.ofHours(24), assertions: Map<String, String>? = null) {
        val decoded = JWT.decode(encoded)
        assertNotNull(decoded)
        assertNotNull(decoded.id)
        assertNotNull(decoded.signature)
        assertNotNull(decoded.issuedAt)
        val iat = decoded.issuedAtAsInstant
        assertTrue(Instant.now().isBefore(iat.plusSeconds(15)))
        val expectedExpires = iat.plus(ttl).truncatedTo(ChronoUnit.SECONDS)
        assertEquals(expectedExpires, decoded.expiresAtAsInstant?.truncatedTo(ChronoUnit.SECONDS))
        val claims = decoded.claims
        assertNotNull(claims)
        assertEquals(applicationId, claims["application_id"]?.asString())
        assertEquals("session.connect", claims["scope"]?.asString())
        assertEquals(sessionId, claims["session_id"]?.asString())
        assertEquals(role.name.lowercase(), claims["role"]?.asString())
        assertions?.forEach { (key, value) -> assertEquals(value, claims[key]?.asString()) }
    }

    @Test
    fun `generate token default parameters`() {
        assertEqualsJwt(existingSession.generateToken())
    }

    @Test
    fun `generate token all parameters`() {
        val data = userName
        val role = Role.SUBSCRIBER
        val ttl = Duration.ofHours(8)

        val encoded = existingSession.generateToken {
            data(data); role(role); expiryLength(ttl)
            initialLayoutClassList(layoutClasses)
        }

        assertEqualsJwt(encoded, role, ttl, mapOf(
            "connection_data" to data,
            "initial_layout_class_list" to layoutClasses.joinToString(separator = " ")
        ))
    }

    @Test
    fun `start audio connector all fields`() {
        mockPost(expectedUrl = "$baseUrl/connect", expectedRequestParams = mapOf(
                "sessionId" to sessionId,
                "token" to token,
                "websocket" to mapOf(
                    "uri" to websocketUri,
                    "streams" to listOf(streamId, randomUuidStr),
                    "headers" to headers,
                    "audioRate" to 16000
                )
            ),
            expectedResponseParams = mapOf(
                "id" to audioConnectorId,
                "connectionId" to connectionId
            )
        )

        val response = client.connectToWebsocket {
            uri(websocketUri); headers(headers)
            sessionId(sessionId); token(token)
            streams(streamId, randomUuidStr)
            audioRate(Websocket.AudioRate.L16_16K)
        }
        assertNotNull(response)
        assertEquals(UUID.fromString(audioConnectorId), response.id)
        assertEquals(UUID.fromString(connectionId), response.connectionId)
    }

    @Test
    fun `start audio connector required fields`() {
        mockPost(expectedUrl = "$baseUrl/connect", expectedRequestParams = mapOf(
            "sessionId" to sessionId, "token" to token,
            "websocket" to mapOf("uri" to websocketUri)
        ),
        expectedResponseParams = mapOf("id" to audioConnectorId))

        val response = client.connectToWebsocket {
            uri(websocketUri); sessionId(sessionId); token(token)
        }
        assertNotNull(response)
        assertEquals(UUID.fromString(audioConnectorId), response.id)
        assertNull(response.connectionId)
    }

    @Test
    fun `start live captions all parameters`() {
        val partialCaptions = true
        mockPost(expectedUrl = "$baseUrl/captions", status = 202,
            expectedRequestParams = mapOf(
                "sessionId" to sessionId,
                "token" to token,
                "languageCode" to "en-US",
                "maxDuration" to maxDuration,
                "partialCaptions" to partialCaptions,
                "statusCallbackUrl" to statusCallbackUrl
            ),
            expectedResponseParams = mapOf("captionsId" to captionsId)
        )
        assertEquals(UUID.fromString(captionsId), existingSession.startCaptions(token) {
            languageCode(Language.EN_US); maxDuration(maxDuration)
            partialCaptions(partialCaptions); statusCallbackUrl(statusCallbackUrl)
        })
    }

    @Test
    fun `stop live captions`() {
        mockPost(expectedUrl = "$baseUrl/captions/$captionsId/stop", status = 202)
        existingSession.stopCaptions(captionsId)
    }

    @Test
    fun `play DTMF into SIP call`() {
        mockPost(expectedUrl = "$sessionUrl/play-dtmf", expectedRequestParams = mapOf("digits" to dtmf))
        existingSession.sendDtmf(dtmf)
    }

    @Test
    fun `send DTMF to specific participant`() {
        mockPost(expectedUrl = "$connectionBaseUrl/play-dtmf", expectedRequestParams = mapOf("digits" to dtmf))
        existingConnection.sendDtmf(dtmf)
    }

    @Test
    fun `initiate outbound SIP call all parameters`() {
        val from = "from@example.com"
        val secure = true
        val video = false
        val observeForceMute = true
        val password = "P@s5w0rd123"

        mockPost(expectedUrl = "$baseUrl/dial", expectedRequestParams = mapOf(
                "sessionId" to sessionId, "token" to token,
                "sip" to mapOf(
                    "uri" to "$sipUri;transport=tls",
                    "from" to from,
                    "headers" to headers,
                    "auth" to mapOf(
                        "username" to userName,
                        "password" to password
                    ),
                    "secure" to secure,
                    "video" to video,
                    "observeForceMute" to observeForceMute
                )
            ),
            expectedResponseParams = mapOf(
                "id" to sipCallId,
                "connectionId" to connectionId,
                "streamId" to streamId
            )
        )
        val response = client.sipDial {
            sessionId(sessionId); token(token)
            uri(URI.create(sipUri), true)
            addHeaders(headers); secure(secure)
            from(from); video(video)
            observeForceMute(observeForceMute)
            username(userName); password(password)
        }
        assertNotNull(response)
        assertEquals(sipCallId, response.id)
        assertEquals(connectionId, response.connectionId)
        assertEquals(streamId, response.streamId)
    }

    @Test
    fun `signal all participants`() {
        mockPost(expectedUrl = "$sessionUrl/signal", expectedRequestParams = signalRequestMap, status = 204)
        existingSession.signalAll(type, data)
    }

    @Test
    fun `signal single participant`() {
        mockPost(expectedUrl = "$connectionBaseUrl/signal", expectedRequestParams = signalRequestMap, status = 204)
        existingConnection.signal(type, data)
    }

    @Test
    fun `force disconnect`() {
        mockDelete(expectedUrl = connectionBaseUrl)
        existingConnection.disconnect()
    }

    @Test
    fun `mute participant stream`() {
        mockPost(expectedUrl = "$streamUrl/mute")
        existingStream.mute()
    }

    @Test
    fun `mute all streams empty response`() {
        mockPost(expectedUrl = "$sessionUrl/mute",
            expectedRequestParams = mapOf("active" to true),
            expectedResponseParams = mapOf()
        )
        val response = existingSession.muteStreams()
        assertNotNull(response)
        assertNull(response.applicationId)
        assertNull(response.status)
        assertNull(response.name)
        assertNull(response.environment)
        assertNull(response.createdAt)
    }

    @Test
    fun `mute selected streams full response`() {
        val active = false
        val status = ProjectStatus.ACTIVE
        val name = "Project Name"
        val environment = ProjectEnvironment.STANDARD

        mockPost(expectedUrl = "$sessionUrl/mute",
            expectedRequestParams = mapOf(
                "active" to active,
                "excludedStreamIds" to listOf(streamId, randomUuidStr)
            ),
            expectedResponseParams = mapOf(
                "applicationId" to applicationId,
                "status" to status.name,
                "name" to name,
                "environment" to environment.name.lowercase(),
                "createdAt" to createdAtLong
            )
        )
        val response = existingSession.muteStreams(active, streamId, randomUuidStr)
        assertNotNull(response)
        assertEquals(applicationId, response.applicationId)
        assertEquals(status, response.status)
        assertEquals(name, response.name)
        assertEquals(environment, response.environment)
        assertEquals(createdAtLong, response.createdAt)
    }

    @Test
    fun `get single stream layout`() {
        mockGet(expectedUrl = streamUrl, expectedResponseParams = streamLayoutMap)
        assertEqualsSampleStream(existingStream.info())
    }

    @Test
    fun `get all stream layouts`() {
        mockGet(expectedUrl = streamBaseUrl, expectedResponseParams = mapOf(
            "count" to 4,
            "items" to listOf(
                mapOf(),
                streamLayoutMap,
                mapOf("id" to randomUuidStr),
                mapOf("layoutClassList" to listOf<String>())
            )
        ))
        val response = existingSession.listStreams()
        assertEquals(4, response.size)
        val empty = response[0]
        assertNotNull(empty)
        assertNull(empty.id)
        assertNull(empty.videoType)
        assertNull(empty.name)
        assertNull(empty.layoutClassList)
        assertEqualsSampleStream(response[1])
        val idOnly = response[2]
        assertNotNull(idOnly)
        assertEquals(UUID.fromString(randomUuidStr), idOnly.id)
        assertNull(idOnly.videoType)
        assertNull(idOnly.name)
        assertNull(idOnly.layoutClassList)
        val emptyLayout = response[3]
        assertNotNull(emptyLayout)
        assertEquals(0, emptyLayout.layoutClassList.size)
    }

    @Test
    fun `change stream layout`() {
        mockPut(expectedUrl = streamBaseUrl, expectedRequestParams = mapOf(
            "items" to listOf(mapOf(
                "id" to streamId,
                "layoutClassList" to layoutClasses
            ))
        ))
        existingStream.setLayout(*layoutClasses.toTypedArray())
    }

    @Test
    fun `create session no parameters`() {
        mockPostQueryParams(
            expectedUrl = "/session/create",
            authType = authType,
            expectedRequestParams = mapOf(),
            expectedResponseParams = listOf(mapOf<String, Any>())
        )
        val response = client.createSession()
        assertNotNull(response)
        assertNull(response.sessionId)
        assertNull(response.applicationId)
        assertNull(response.createDt)
        assertNull(response.mediaServerUrl)
    }

    @Test
    fun `create session all parameters`() {
        val location = "127.0.0.1"
        val archiveMode = ArchiveMode.ALWAYS

        mockPostQueryParams(
            expectedUrl = "/session/create",
            authType = authType,
            expectedRequestParams = mapOf(
                "archiveMode" to archiveMode.name.lowercase(),
                "location" to location,
                "p2p.preference" to "disabled"
            ),
            expectedResponseParams = listOf(mapOf(
                "session_id" to sessionId,
                "application_id" to applicationId,
                "create_dt" to createdAtLong,
                "media_server_url" to mediaUrl
            ))
        )
        val response = client.createSession {
            location(location)
            mediaMode(MediaMode.ROUTED)
            archiveMode(archiveMode)
        }
        assertNotNull(response)
        assertEquals(sessionId, response.sessionId)
        assertEquals(UUID.fromString(applicationId), response.applicationId)
        assertEquals(createdAtLong.toString(), response.createDt)
        assertEquals(URI.create(mediaUrl), response.mediaServerUrl)
    }

    @Test
    fun `create archive all parameters`() {

    }

    @Test
    fun `create broadcast all parameters`() {

    }

    @Test
    fun `list archives both parameters`() {

    }

    @Test
    fun `list archives no parameters`() {

    }

    @Test
    fun `list broadcasts both parameters`() {

    }

    @Test
    fun `list broadcasts no parameters`() {

    }

    @Test
    fun `get archive`() {
        mockGet(expectedUrl = archiveBaseUrl, expectedResponseParams = archiveResponseMap)
        assertEqualsSampleArchive(existingArchive.info())
    }

    @Test
    fun `stop archive`() {
        mockPost(expectedUrl = "$archiveBaseUrl/stop", expectedResponseParams = archiveResponseMap)
        assertEqualsSampleArchive(existingArchive.stop())
    }

    @Test
    fun `delete archive`() {
        mockDelete(expectedUrl = archiveBaseUrl, authType = authType)
        existingArchive.delete()
    }

    @Test
    fun `add archive stream id only`() {
        mockPatch(
            expectedUrl = archiveStreamsUrl,
            expectedRequestParams = addStreamMap(),
            authType = authType, status = 204
        )
        existingArchive.addStream(streamId)
    }

    @Test
    fun `add archive stream audio and video`() {
        val audio = true; val video = false
        mockPatch(
            expectedUrl = archiveStreamsUrl,
            expectedRequestParams = addStreamMap(audio = audio, video = video),
            authType = authType, status = 204
        )
        existingArchive.addStream(streamId, audio = audio, video = video)
    }

    @Test
    fun `remove archive stream`() {
        mockPatch(
            expectedUrl = archiveStreamsUrl,
            expectedRequestParams = removeStreamMap,
            authType = authType, status = 204
        )
        existingArchive.removeStream(streamId)
    }

    @Test
    fun `change archive layout vertical`() {
        mockPut(expectedUrl = archiveLayoutUrl, expectedRequestParams = mapOf("type" to "verticalPresentation"))
        existingArchive.setLayout(ScreenLayoutType.VERTICAL)
    }

    @Test
    fun `change archive layout pip`() {
        mockPut(expectedUrl = archiveLayoutUrl, expectedRequestParams = pipLayoutMap)
        existingArchive.setLayout(ScreenLayoutType.BEST_FIT, ScreenLayoutType.PIP)
    }

    @Test
    fun `change archive layout stylesheet`() {
        mockPut(expectedUrl = archiveLayoutUrl, expectedRequestParams = customLayoutMap)
        existingArchive.setLayout(ScreenLayoutType.CUSTOM, stylesheet = stylesheet)
    }

    @Test
    fun `get broadcast`() {
        mockGet(expectedUrl = broadcastBaseUrl, expectedResponseParams = broadcastResponseMap)
        assertEqualsSampleBroadcast(existingBroadcast.info())
    }

    @Test
    fun `stop broadcast`() {
        mockPost(expectedUrl = "$broadcastBaseUrl/stop", expectedResponseParams = broadcastResponseMap)
        assertEqualsSampleBroadcast(existingBroadcast.stop())
    }

    @Test
    fun `add broadcast stream id only`() {
        mockPatch(
            expectedUrl = broadcastStreamsUrl,
            expectedRequestParams = addStreamMap(),
            authType = authType, status = 204
        )
        existingBroadcast.addStream(streamId)
    }

    @Test
    fun `add broadcast stream audio and video`() {
        val audio = false; val video = true
        mockPatch(
            expectedUrl = broadcastStreamsUrl,
            expectedRequestParams = addStreamMap(audio = audio, video = video),
            authType = authType, status = 204
        )
        existingBroadcast.addStream(streamId, audio = audio, video = video)
    }

    @Test
    fun `remove broadcast stream`() {
        mockPatch(
            expectedUrl = broadcastStreamsUrl,
            expectedRequestParams = removeStreamMap,
            authType = authType, status = 204
        )
        existingBroadcast.removeStream(streamId)
    }

    @Test
    fun `change broadcast layout horizontal`() {
        mockPut(expectedUrl = broadcastLayoutUrl, expectedRequestParams = mapOf("type" to "horizontalPresentation"))
        existingBroadcast.setLayout(ScreenLayoutType.HORIZONTAL)
    }

    @Test
    fun `change broadcast layout pip`() {
        mockPut(expectedUrl = broadcastLayoutUrl, expectedRequestParams = pipLayoutMap)
        existingBroadcast.setLayout(ScreenLayoutType.BEST_FIT, ScreenLayoutType.PIP)
    }

    @Test
    fun `change broadcast layout stylesheet`() {
        mockPut(expectedUrl = broadcastLayoutUrl, expectedRequestParams = customLayoutMap)
        existingBroadcast.setLayout(ScreenLayoutType.CUSTOM, stylesheet = stylesheet)
    }

    @Test
    fun `stop experience composer`() {
        mockDelete(expectedUrl = renderUrl, authType = authType)
        existingRender.stop()
    }

    @Test
    fun `get experience composer`() {
        mockGet(expectedUrl = renderUrl, expectedResponseParams = renderResponseMap)
        assertEqualsSampleRender(existingRender.info())
    }

    @Test
    fun `list experience composers no parameters`() {
        mockGet(expectedUrl = renderBaseUrl,
            expectedQueryParams = defaultOffsetCountMap,
            expectedResponseParams = mapOf(
                "count" to count,
                "items" to listOf<Map<String, Any>>()
            )
        )
        val response = client.listRenders()
        assertNotNull(response)
        assertEquals(0, response.size)
    }

    @Test
    fun `list experience composers both parameters`() {
        mockGet(expectedUrl = renderBaseUrl,
            expectedQueryParams = customOffsetCountMap,
            expectedResponseParams = mapOf(
                "count" to 2,
                "items" to listOf(renderResponseMap, mapOf())
            )
        )
        val response = client.listRenders(count, offset)
        assertNotNull(response)
        assertEquals(2, response.size)
        assertEqualsSampleRender(response[0])
        val empty = response[1]
        assertNotNull(empty)
        assertNull(empty.id)
        assertNull(empty.sessionId)
        assertNull(empty.applicationId)
        assertNull(empty.createdAt)
        assertNull(empty.callbackUrl)
        assertNull(empty.updatedAt)
        assertNull(empty.url)
        assertNull(empty.resolution)
        assertNull(empty.status)
        assertNull(empty.streamId)
    }

    @Test
    fun `start experience composer all parameters`() {
        mockPost(expectedUrl = renderBaseUrl, expectedRequestParams = mapOf(
                "sessionId" to sessionId,
                "token" to token,
                "url" to mediaUrl,
                "maxDuration" to maxDuration,
                "resolution" to "1280x720",
                "properties" to mapOf("name" to renderName)
            ),
            expectedResponseParams = renderResponseMap
        )
        assertEqualsSampleRender(client.startRender {
            url(mediaUrl); maxDuration(maxDuration)
            resolution(Resolution.HD_LANDSCAPE)
            sessionId(sessionId); token(token); name(renderName)
        })
    }
}