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
import com.vonage.client.common.HttpMethod
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
    private val createSessionUrl = "/session/create"
    private val sessionUrl = "$baseUrl/session/$sessionId"
    private val connectionBaseUrl = "$sessionUrl/connection/$connectionId"
    private val streamBaseUrl = "$sessionUrl/stream"
    private val streamUrl = "$streamBaseUrl/$streamId"
    private val archiveBaseUrl = "$baseUrl/archive"
    private val archiveUrl = "$archiveBaseUrl/$archiveId"
    private val broadcastBaseUrl = "$baseUrl/broadcast"
    private val broadcastUrl = "$broadcastBaseUrl/$broadcastId"
    private val broadcastLayoutUrl = "$broadcastUrl/layout"
    private val broadcastStreamsUrl = "$broadcastUrl/streams"
    private val archiveLayoutUrl = "$archiveUrl/layout"
    private val archiveStreamsUrl = "$archiveUrl/streams"
    private val audioConnectorUrl = "$baseUrl/connect"
    private val captionsBaseUrl = "$baseUrl/captions"
    private val renderBaseUrl = "$baseUrl/render"
    private val renderUrl = "$renderBaseUrl/$renderId"
    private val sipDialUrl = "$baseUrl/dial"
    private val muteSessionUrl = "$sessionUrl/mute"
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
    private val sessionIdMap = mapOf("sessionId" to sessionId)
    private val sessionIdTokenMap = sessionIdMap + mapOf("token" to token)
    private val customSessionOffsetCountMap = sessionIdMap + customOffsetCountMap
    private val defaultSessionOffsetCountMap = sessionIdMap + defaultOffsetCountMap
    private val layoutClasses = listOf("full", "no-border")
    private val dtmfMap = mapOf("digits" to dtmf)
    private val streamLayoutMap = mapOf(
        "id" to streamId,
        "videoType" to videoType.name.lowercase(),
        "name" to streamName,
        "layoutClassList" to layoutClasses
    )
    private val renderResponseMap = sessionIdMap + mapOf(
        "id" to renderId,
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
    private val archiveResolution = Resolution.FHD_LANDSCAPE
    private val dvr = false
    private val lowLatency = true
    private val hlsUrl = "https://hls.example.com/stream.m3u8"
    private val broadcastAudio = true
    private val broadcastVideo = false
    private val archiveHasAudio = false
    private val archiveHasVideo = true
    private val broadcastStreamMode = StreamMode.MANUAL
    private val archiveStreamMode = StreamMode.AUTO
    private val archiveOutputMode = OutputMode.COMPOSED
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
    private val hlsMap = "hls" to mapOf(
        "dvr" to dvr,
        "lowLatency" to lowLatency
    )
    private val rtmpRequestMap = mapOf(
        "id" to rtmpId,
        "serverUrl" to rtmpServerUrl,
        "streamName" to streamName
    )
    private val rtmpResponseMap = rtmpRequestMap + mapOf("status" to rtmpStatus.name.lowercase())
    private val archiveVideoUrl = "https://tokbox.com.archive2.s3.amazonaws.com/123456/$archiveId/archive.mp4"
    private val archiveBaseMap = sessionIdMap + mapOf(
        "name" to archiveName,
        "hasAudio" to archiveHasAudio,
        "hasVideo" to archiveHasVideo,
        "maxBitrate" to maxBitrate,
        "multiArchiveTag" to multiArchiveTag,
        "resolution" to archiveResolutionStr,
        "streamMode" to archiveStreamMode.name.lowercase()
    )
    private val archiveRequestMap = archiveBaseMap + mapOf(
        "outputMode" to archiveOutputMode.name.lowercase(),
        "layout" to customLayoutMap
    )
    private val archiveResponseMap = archiveBaseMap + mapOf(
        "id" to archiveId,
        "applicationId" to applicationId,
        "createdAt" to createdAtLong,
        "duration" to archiveDuration,
        "size" to archiveSize,
        "status" to archiveStatus.name.lowercase(),
        "url" to archiveVideoUrl,
        "streams" to streamsList
    )
    private val broadcastBaseMap = sessionIdMap + mapOf(
        "multiBroadcastTag" to multiBroadcastTag,
        "maxDuration" to maxDuration,
        "maxBitrate" to maxBitrate,
        "resolution" to broadcastResolutionStr,
        "streamMode" to broadcastStreamMode.name.lowercase()
    )
    private val broadcastRequestMap = broadcastBaseMap + mapOf(
        "layout" to pipLayoutMap,
        "outputs" to mapOf(
            hlsMap, "rtmp" to listOf(rtmpRequestMap)
        )
    )
    private val broadcastResponseMap = broadcastBaseMap + mapOf(
        "id" to broadcastId,
        "applicationId" to applicationId,
        "createdAt" to createdAtLong,
        "updatedAt" to updatedAtLong,
        "broadcastUrls" to mapOf(
            "hls" to hlsUrl,
            "rtmp" to listOf(rtmpResponseMap, emptyMap())
        ),
        "settings" to mapOf(hlsMap),
        "hasAudio" to broadcastAudio,
        "hasVideo" to broadcastVideo,
        "status" to broadcastStatus.name.lowercase(),
        "streams" to streamsList
    )
    private val renderRequestMap = sessionIdTokenMap + mapOf(
        "url" to mediaUrl,
        "properties" to mapOf("name" to renderName)
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

    private fun assertEqualsSampleRender(render: RenderResponse) {
        assertNotNull(render)
        assertEquals(UUID.fromString(renderId), render.id)
        assertEquals(sessionId, render.sessionId)
        assertEquals(UUID.fromString(applicationId), render.applicationId)
        assertEquals(createdAtLong, render.createdAt)
        assertEquals(URI.create(statusCallbackUrl), render.callbackUrl)
        assertEquals(updatedAtLong, render.updatedAt)
        assertEquals(URI.create(mediaUrl), render.url)
        assertEquals(Resolution.SD_PORTRAIT, render.resolution)
        assertEquals(RenderStatus.STARTING, render.status)
        assertEquals(UUID.fromString(streamId), render.streamId)
    }

    private fun assertEqualsEmptyRender(render: RenderResponse) {
        assertNotNull(render)
        assertNull(render.id)
        assertNull(render.sessionId)
        assertNull(render.applicationId)
        assertNull(render.createdAt)
        assertNull(render.callbackUrl)
        assertNull(render.updatedAt)
        assertNull(render.url)
        assertNull(render.resolution)
        assertNull(render.status)
        assertNull(render.streamId)
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

    private fun assertEqualsSampleArchive(archive: Archive) {
        assertNotNull(archive)
        assertEquals(UUID.fromString(archiveId), archive.id)
        assertEquals(sessionId, archive.sessionId)
        assertEquals(UUID.fromString(applicationId), archive.applicationId)
        assertEquals(multiArchiveTag, archive.multiArchiveTag)
        assertEquals(archiveName, archive.name)
        assertEquals(createdAtInstant, archive.createdAt)
        assertEquals(Duration.ofSeconds(archiveDuration), archive.duration)
        assertEquals(archiveSize, archive.size)
        assertEquals(maxBitrate, archive.maxBitrate)
        assertEquals(archiveStatus, archive.status)
        assertEquals(archiveStreamMode, archive.streamMode)
        assertEquals(archiveResolution, archive.resolution)
        assertEquals(URI.create(archiveVideoUrl), archive.url)
        assertTrue(archive.hasVideo())
        assertFalse(archive.hasAudio())
        assertEqualsVideoStreams(archive.streams)
    }

    private fun assertEqualsSampleBroadcast(broadcast: Broadcast) {
        assertNotNull(broadcast)
        assertEquals(UUID.fromString(broadcastId), broadcast.id)
        assertEquals(sessionId, broadcast.sessionId)
        assertEquals(UUID.fromString(applicationId), broadcast.applicationId)
        assertEquals(multiBroadcastTag, broadcast.multiBroadcastTag)
        assertEquals(createdAtInstant, broadcast.createdAt)
        assertEquals(updatedAtInstant, broadcast.updatedAt)
        assertEquals(Duration.ofSeconds(maxDuration.toLong()), broadcast.maxDuration)
        assertEquals(maxBitrate, broadcast.maxBitrate)
        val broadcastUrls = broadcast.broadcastUrls
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
        val hls = broadcast.hlsSettings
        assertNotNull(hls)
        assertEquals(dvr, hls.dvr())
        assertEquals(lowLatency, hls.lowLatency())
        assertEquals(broadcastResolution, broadcast.resolution)
        assertEquals(broadcastAudio, broadcast.hasAudio())
        assertEquals(broadcastVideo, broadcast.hasVideo())
        assertEquals(broadcastStreamMode, broadcast.streamMode)
        assertEquals(broadcastStatus, broadcast.status)
        assertEqualsVideoStreams(broadcast.streams)
    }

    private fun assertEqualsEmptyArchive(archive: Archive) {
        assertNotNull(archive)
        assertNull(archive.id)
        assertNull(archive.sessionId)
        assertNull(archive.applicationId)
        assertNull(archive.createdAt)
        assertNull(archive.multiArchiveTag)
        assertNull(archive.name)
        assertNull(archive.duration)
        assertNull(archive.size)
        assertNull(archive.status)
        assertNull(archive.streamMode)
        assertNull(archive.resolution)
        assertNull(archive.url)
        assertNull(archive.hasAudio())
        assertNull(archive.hasVideo())
        assertNull(archive.streams)
    }

    private fun assertEqualsEmptyBroadcast(broadcast: Broadcast) {
        assertNotNull(broadcast)
        assertNull(broadcast.id)
        assertNull(broadcast.sessionId)
        assertNull(broadcast.applicationId)
        assertNull(broadcast.multiBroadcastTag)
        assertNull(broadcast.createdAt)
        assertNull(broadcast.updatedAt)
        assertNull(broadcast.maxDuration)
        assertNull(broadcast.maxBitrate)
        assertNull(broadcast.broadcastUrls)
        assertNull(broadcast.hlsSettings)
        assertNull(broadcast.resolution)
        assertNull(broadcast.hasAudio())
        assertNull(broadcast.hasVideo())
        assertNull(broadcast.streamMode)
        assertNull(broadcast.status)
        assertNull(broadcast.streams)
    }

    private fun assertListArchives(params: Map<String, Any>, invocation: () -> List<Archive>) {
        mockGet(expectedUrl = archiveBaseUrl, authType = authType,
            expectedQueryParams = params, expectedResponseParams = mapOf(
                "count" to count,
                "items" to listOf(archiveResponseMap, mapOf())
            )
        )
        val response = invocation()
        assertEquals(2, response.size)
        assertEqualsSampleArchive(response[0])
        assertEqualsEmptyArchive(response[1])
        assertApiResponseException<VideoResponseException>(archiveBaseUrl, HttpMethod.GET, invocation)
    }

    private fun assertListBroadcasts(params: Map<String, Any>, invocation: () -> List<Broadcast>) {
        mockGet(expectedUrl = broadcastBaseUrl, authType = authType,
            expectedQueryParams = params, expectedResponseParams = mapOf(
                "count" to count,
                "items" to listOf(broadcastResponseMap, mapOf())
            )
        )
        val response = invocation()
        assertEquals(2, response.size)
        assertEqualsSampleBroadcast(response[0])
        assertEqualsEmptyBroadcast(response[1])
        assertApiResponseException<VideoResponseException>(broadcastBaseUrl, HttpMethod.GET, invocation)
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
        mockPost(expectedUrl = audioConnectorUrl, authType = authType,
            expectedRequestParams = sessionIdTokenMap + mapOf(
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

        val response = existingSession.connectToWebsocket {
            uri(websocketUri); headers(headers)
            streams(streamId, randomUuidStr); token(token)
            audioRate(Websocket.AudioRate.L16_16K)
        }
        assertNotNull(response)
        assertEquals(UUID.fromString(audioConnectorId), response.id)
        assertEquals(UUID.fromString(connectionId), response.connectionId)
    }

    @Test
    fun `start audio connector required fields`() {
        mockPost(expectedUrl = audioConnectorUrl, authType = authType,
            expectedRequestParams = sessionIdTokenMap + mapOf(
                "websocket" to mapOf("uri" to websocketUri)
            ),
            expectedResponseParams = mapOf("id" to audioConnectorId)
        )

        val invocation = { existingSession.connectToWebsocket {
            uri(websocketUri); token(token)
        }}
        val response = invocation()
        assertNotNull(response)
        assertEquals(UUID.fromString(audioConnectorId), response.id)
        assertNull(response.connectionId)

        assertApiResponseException<VideoResponseException>(audioConnectorUrl, HttpMethod.POST, invocation)
    }

    @Test
    fun `start live captions all parameters`() {
        val partialCaptions = true
        mockPost(expectedUrl = captionsBaseUrl, status = 202, authType = authType,
            expectedRequestParams = sessionIdTokenMap + mapOf(
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
    fun `start live captions required parameters`() {
        mockPost(expectedUrl = captionsBaseUrl, status = 202, authType = authType,
            expectedRequestParams = sessionIdTokenMap,
            expectedResponseParams = mapOf("captionsId" to captionsId)
        )
        val invocation = { existingSession.startCaptions(token) }
        assertEquals(UUID.fromString(captionsId), invocation())
        assertApiResponseException<VideoResponseException>(captionsBaseUrl, HttpMethod.POST, invocation)
    }

    @Test
    fun `stop live captions`() {
        val url = "$captionsBaseUrl/$captionsId/stop"
        mockPost(expectedUrl = url, status = 202, authType = authType)
        val invocation = { existingSession.stopCaptions(captionsId) }
        invocation()
        assertApiResponseException<VideoResponseException>(url, HttpMethod.POST, invocation)
    }

    @Test
    fun `play DTMF into SIP call`() {
        val url = "$sessionUrl/play-dtmf"
        mockPost(expectedUrl = url, expectedRequestParams = dtmfMap, authType = authType)
        val invocation = { existingSession.sendDtmf(dtmf) }
        invocation()
        assertApiResponseException<VideoResponseException>(url, HttpMethod.POST, invocation)
    }

    @Test
    fun `send DTMF to specific participant`() {
        val url = "$connectionBaseUrl/play-dtmf"
        mockPost(expectedUrl = url, expectedRequestParams = dtmfMap, authType = authType)
        val invocation = { existingConnection.sendDtmf(dtmf) }
        invocation()
        assertApiResponseException<VideoResponseException>(url, HttpMethod.POST, invocation)
    }

    @Test
    fun `sip dial all parameters`() {
        val from = "from@example.com"
        val secure = true
        val video = false
        val observeForceMute = true
        val password = "P@s5w0rd123"

        mockPost(expectedUrl = sipDialUrl, authType = authType,
            expectedRequestParams = sessionIdTokenMap + mapOf(
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
        val response = existingSession.sipDial {
            uri(URI.create(sipUri), true)
            addHeaders(headers); secure(secure)
            from(from); video(video); token(token)
            observeForceMute(observeForceMute)
            username(userName); password(password)
        }
        assertNotNull(response)
        assertEquals(sipCallId, response.id)
        assertEquals(connectionId, response.connectionId)
        assertEquals(streamId, response.streamId)
    }

    @Test
    fun `sip dial required parameters`() {
        mockPost(
            expectedUrl = sipDialUrl, authType = authType,
            expectedRequestParams = sessionIdTokenMap + mapOf(
                "sip" to mapOf("uri" to sipUri)
            ),
            expectedResponseParams = mapOf("id" to sipCallId)
        )
        val invocation = { existingSession.sipDial {
            sessionId(sessionId); token(token); uri(URI.create(sipUri), false)
        } }
        val response = invocation()
        assertNotNull(response)
        assertEquals(sipCallId, response.id)
        assertNull(response.connectionId)
        assertNull(response.streamId)
        assertApiResponseException<VideoResponseException>(sipDialUrl, HttpMethod.POST, invocation)
    }

    @Test
    fun `signal all participants`() {
        val url = "$sessionUrl/signal"
        mockPost(expectedUrl = url, expectedRequestParams = signalRequestMap, status = 204, authType = authType)
        val invocation = { existingSession.signalAll(type, data) }
        invocation()
        assertApiResponseException<VideoResponseException>(url, HttpMethod.POST, invocation)
    }

    @Test
    fun `signal single participant`() {
        val url = "$connectionBaseUrl/signal"
        mockPost(expectedUrl = url, expectedRequestParams = signalRequestMap, status = 204, authType = authType)
        val invocation = { existingConnection.signal(type, data) }
        invocation()
        assertApiResponseException<VideoResponseException>(url, HttpMethod.POST, invocation)
    }

    @Test
    fun `force disconnect`() {
        mockDelete(expectedUrl = connectionBaseUrl, authType = authType)
        existingConnection.disconnect()
        assertApiResponseException<VideoResponseException>(
            connectionBaseUrl, HttpMethod.DELETE, existingConnection::disconnect
        )
    }

    @Test
    fun `mute participant stream`() {
        val url = "$streamUrl/mute"
        mockPost(expectedUrl = url, authType = authType)
        existingStream.mute()
        assertApiResponseException<VideoResponseException>(
            url, HttpMethod.POST, existingStream::mute
        )
    }

    @Test
    fun `mute all streams empty response`() {
        mockPost(expectedUrl = muteSessionUrl, authType = authType,
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
        assertApiResponseException<VideoResponseException>(
            muteSessionUrl, HttpMethod.POST, existingSession::muteStreams
        )
    }

    @Test
    fun `mute selected streams full response`() {
        val active = false
        val status = ProjectStatus.ACTIVE
        val name = "Project Name"
        val environment = ProjectEnvironment.STANDARD

        mockPost(expectedUrl = muteSessionUrl, authType = authType,
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
        val invocation = { existingSession.muteStreams(active, streamId, randomUuidStr) }
        val response = invocation()
        assertNotNull(response)
        assertEquals(applicationId, response.applicationId)
        assertEquals(status, response.status)
        assertEquals(name, response.name)
        assertEquals(environment, response.environment)
        assertEquals(createdAtLong, response.createdAt)

        assertApiResponseException<VideoResponseException>(muteSessionUrl, HttpMethod.POST, invocation)
    }

    @Test
    fun `get single stream layout`() {
        mockGet(expectedUrl = streamUrl, expectedResponseParams = streamLayoutMap, authType = authType)
        assertEqualsSampleStream(existingStream.info())
        assertApiResponseException<VideoResponseException>(streamUrl, HttpMethod.GET, existingStream::info)
    }

    @Test
    fun `get all stream layouts`() {
        mockGet(expectedUrl = streamBaseUrl, authType = authType,
            expectedResponseParams = mapOf(
                "count" to 4,
                "items" to listOf(
                    mapOf(),
                    streamLayoutMap,
                    mapOf("id" to randomUuidStr),
                    mapOf("layoutClassList" to listOf<String>())
                )
            )
        )
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

        assertApiResponseException<VideoResponseException>(
            streamBaseUrl, HttpMethod.GET, existingSession::listStreams
        )
    }

    @Test
    fun `change stream layout`() {
        mockPut(expectedUrl = streamBaseUrl, authType = authType,
            expectedRequestParams = mapOf(
                "items" to listOf(mapOf(
                    "id" to streamId,
                    "layoutClassList" to layoutClasses
                ))
            )
        )
        val invocation = { existingStream.setLayout(*layoutClasses.toTypedArray()) }
        invocation()
        assertApiResponseException<VideoResponseException>(streamBaseUrl, HttpMethod.PUT, invocation)
    }

    @Test
    fun `create session no parameters`() {
        mockPostQueryParams(expectedUrl = createSessionUrl, authType = authType,
            expectedRequestParams = mapOf(),
            expectedResponseParams = listOf(mapOf<String, Any>())
        )
        val response = client.createSession()
        assertNotNull(response)
        assertNull(response.sessionId)
        assertNull(response.applicationId)
        assertNull(response.createDt)
        assertNull(response.mediaServerUrl)

        assertApiResponseException<VideoResponseException>(
            createSessionUrl, HttpMethod.POST, client::createSession
        )
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
    fun `list archives for session no parameters`() {
        assertListArchives(defaultSessionOffsetCountMap, existingSession::listArchives)
    }

    @Test
    fun `list archives for session both parameters`() {
        assertListArchives(customSessionOffsetCountMap) {
            existingSession.listArchives(count, offset)
        }
    }

    @Test
    fun `list archives both parameters`() {
        assertListArchives(customOffsetCountMap) {
            client.listArchives(count, offset)
        }
    }

    @Test
    fun `list archives default parameters`() {
        assertListArchives(defaultOffsetCountMap, client::listArchives)
    }

    @Test
    fun `list broadcasts for session no parameters`() {
        assertListBroadcasts(defaultSessionOffsetCountMap, existingSession::listBroadcasts)
    }

    @Test
    fun `list broadcasts for session both parameters`() {
        assertListBroadcasts(customSessionOffsetCountMap) {
            existingSession.listBroadcasts(count, offset)
        }
    }

    @Test
    fun `list broadcasts both parameters`() {
        assertListBroadcasts(customOffsetCountMap) {
            client.listBroadcasts(count, offset)
        }
    }

    @Test
    fun `list broadcasts default parameters`() {
        assertListBroadcasts(defaultOffsetCountMap, client::listBroadcasts)
    }

    @Test
    fun `create archive required parameters`() {
        mockPost(expectedUrl = archiveBaseUrl, authType = authType,
            expectedRequestParams = sessionIdMap,
            expectedResponseParams = sessionIdMap
        )
        val response = existingSession.createArchive()
        assertNotNull(response)
        assertEquals(sessionId, response.sessionId)

        assertApiResponseException<VideoResponseException>(
            archiveBaseUrl, HttpMethod.POST, existingSession::createArchive
        )
    }

    @Test
    fun `create archive all parameters`() {
        mockPost(expectedUrl = "$baseUrl/archive", authType = authType,
            expectedRequestParams = archiveRequestMap,
            expectedResponseParams = archiveResponseMap
        )
        assertEqualsSampleArchive(existingSession.createArchive {
            name(archiveName); resolution(archiveResolution)
            multiArchiveTag(multiArchiveTag); maxBitrate(maxBitrate)
            hasVideo(archiveHasVideo); hasAudio(archiveHasAudio)
            streamMode(archiveStreamMode); outputMode(archiveOutputMode)
            standardLayout(ScreenLayoutType.HORIZONTAL)
            screenshareLayout(ScreenLayoutType.BEST_FIT)
            customLayout(stylesheet)
        })
    }

    @Test
    fun `create broadcast required parameters`() {
        mockPost(expectedUrl = broadcastBaseUrl, authType = authType,
            expectedRequestParams = sessionIdMap + mapOf(
                "outputs" to mapOf("hls" to emptyMap<String, Any>())
            ),
            expectedResponseParams = sessionIdMap + mapOf("broadcastUrls" to mapOf("hls" to hlsUrl))
        )
        val invocation =  { existingSession.startBroadcast { hls() } }
        val response = invocation()
        assertNotNull(response)
        assertEquals(sessionId, response.sessionId)
        assertNull(response.hlsSettings)
        assertNotNull(response.broadcastUrls)
        assertEquals(URI.create(hlsUrl), response.broadcastUrls.hls)

        assertApiResponseException<VideoResponseException>(broadcastBaseUrl, HttpMethod.POST, invocation)
    }

    @Test
    fun `create broadcast all parameters`() {
        mockPost(expectedUrl = "$baseUrl/broadcast", authType = authType,
            expectedRequestParams = broadcastRequestMap,
            expectedResponseParams = broadcastResponseMap
        )
        assertEqualsSampleBroadcast(existingSession.startBroadcast {
            multiBroadcastTag(multiBroadcastTag)
            maxDuration(maxDuration); maxBitrate(maxBitrate)
            resolution(broadcastResolution); streamMode(broadcastStreamMode)
            standardLayout(ScreenLayoutType.VERTICAL); customLayout(stylesheet)
            screenshareLayout(ScreenLayoutType.PIP)
            hls {
                dvr(dvr); lowLatency(lowLatency)
            }
            addRtmpStream {
                id(rtmpId); serverUrl(rtmpServerUrl); streamName(streamName)
            }
        })
    }

    @Test
    fun `get archive`() {
        mockGet(expectedUrl = archiveUrl, expectedResponseParams = archiveResponseMap, authType = authType)
        assertEqualsSampleArchive(existingArchive.info())
        assertApiResponseException<VideoResponseException>(archiveUrl, HttpMethod.GET, existingArchive::info)
    }

    @Test
    fun `stop archive`() {
        val url = "$archiveUrl/stop"
        mockPost(expectedUrl = url, expectedResponseParams = archiveResponseMap, authType = authType)
        assertEqualsSampleArchive(existingArchive.stop())
        assertApiResponseException<VideoResponseException>(
            url, HttpMethod.POST, existingArchive::stop
        )
    }

    @Test
    fun `delete archive`() {
        mockDelete(expectedUrl = archiveUrl, authType = authType)
        existingArchive.delete()
        assertApiResponseException<VideoResponseException>(
            archiveUrl, HttpMethod.DELETE, existingArchive::delete
        )
    }

    @Test
    fun `add archive stream id only`() {
        mockPatch(
            expectedUrl = archiveStreamsUrl,
            expectedRequestParams = addStreamMap(),
            authType = authType, status = 204
        )
        val invocation = { existingArchive.addStream(streamId) }
        invocation()
        assertApiResponseException<VideoResponseException>(archiveStreamsUrl, HttpMethod.PATCH, invocation)
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
        val invocation = { existingArchive.removeStream(streamId) }
        invocation()
        assertApiResponseException<VideoResponseException>(archiveStreamsUrl, HttpMethod.PATCH, invocation)
    }

    @Test
    fun `change archive layout vertical`() {
        mockPut(expectedUrl = archiveLayoutUrl, authType = authType,
            expectedRequestParams = mapOf("type" to "verticalPresentation")
        )
        val invocation = { existingArchive.setLayout(ScreenLayoutType.VERTICAL) }
        invocation()
        assertApiResponseException<VideoResponseException>(archiveLayoutUrl, HttpMethod.PUT, invocation)
    }

    @Test
    fun `change archive layout pip`() {
        mockPut(expectedUrl = archiveLayoutUrl, expectedRequestParams = pipLayoutMap, authType = authType)
    }

    @Test
    fun `change archive layout stylesheet`() {
        mockPut(expectedUrl = archiveLayoutUrl, expectedRequestParams = customLayoutMap)
        existingArchive.setLayout(ScreenLayoutType.CUSTOM, stylesheet = stylesheet)
    }

    @Test
    fun `get broadcast`() {
        mockGet(expectedUrl = broadcastUrl, expectedResponseParams = broadcastResponseMap, authType = authType)
        assertEqualsSampleBroadcast(existingBroadcast.info())
        assertApiResponseException<VideoResponseException>(
            broadcastUrl, HttpMethod.GET, existingBroadcast::info
        )
    }

    @Test
    fun `stop broadcast`() {
        val url = "$broadcastUrl/stop"
        mockPost(expectedUrl = url, expectedResponseParams = broadcastResponseMap)
        assertEqualsSampleBroadcast(existingBroadcast.stop())
        assertApiResponseException<VideoResponseException>(url, HttpMethod.POST, existingBroadcast::stop)
    }

    @Test
    fun `add broadcast stream id only`() {
        mockPatch(
            expectedUrl = broadcastStreamsUrl,
            expectedRequestParams = addStreamMap(),
            authType = authType, status = 204
        )
        val invocation = { existingBroadcast.addStream(streamId) }
        invocation()
        assertApiResponseException<VideoResponseException>(broadcastStreamsUrl, HttpMethod.PATCH, invocation)
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
        val invocation = { existingBroadcast.removeStream(streamId) }
        invocation()
        assertApiResponseException<VideoResponseException>(broadcastStreamsUrl, HttpMethod.PATCH, invocation)
    }

    @Test
    fun `change broadcast layout horizontal`() {
        mockPut(expectedUrl = broadcastLayoutUrl, authType = authType,
            expectedRequestParams = mapOf("type" to "horizontalPresentation")
        )
        val invocation = { existingBroadcast.setLayout(ScreenLayoutType.HORIZONTAL) }
        invocation()
        assertApiResponseException<VideoResponseException>(broadcastLayoutUrl, HttpMethod.PUT, invocation)
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
        assertApiResponseException<VideoResponseException>(
            renderUrl, HttpMethod.DELETE, existingRender::stop
        )
    }

    @Test
    fun `get experience composer`() {
        mockGet(expectedUrl = renderUrl, expectedResponseParams = renderResponseMap)
        assertEqualsSampleRender(existingRender.info())
        assertApiResponseException<VideoResponseException>(
            renderUrl, HttpMethod.GET, existingRender::info
        )
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

        assertApiResponseException<VideoResponseException>(
            renderBaseUrl, HttpMethod.GET, client::listRenders
        )
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
        assertEqualsEmptyRender(response[1])
    }

    @Test
    fun `start experience composer required parameters`() {
        mockPost(expectedUrl = renderBaseUrl, authType = authType,
            expectedRequestParams = renderRequestMap,
            expectedResponseParams = mapOf()
        )
        val invocation = { existingSession.startRender {
            token(token); url(mediaUrl); name(renderName)
        } }
        assertEqualsEmptyRender(invocation())
        assertApiResponseException<VideoResponseException>(renderBaseUrl, HttpMethod.POST, invocation)
    }

    @Test
    fun `start experience composer all parameters`() {
        mockPost(expectedUrl = renderBaseUrl, authType = authType,
            expectedRequestParams = renderRequestMap + mapOf(
                "maxDuration" to maxDuration,
                "resolution" to "1280x720",
            ),
            expectedResponseParams = renderResponseMap
        )
        assertEqualsSampleRender(existingSession.startRender {
            url(mediaUrl); maxDuration(maxDuration)
            resolution(Resolution.HD_LANDSCAPE)
            sessionId(sessionId); token(token); name(renderName)
        })
    }
}