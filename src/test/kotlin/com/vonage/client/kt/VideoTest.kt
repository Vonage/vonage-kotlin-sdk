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
import java.net.URI
import java.util.*
import kotlin.test.*

class VideoTest : AbstractTest() {
    private val client = vonage.video
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
    private val sessionBaseUrl = "$baseUrl/session/$sessionId"
    private val connectionBaseUrl = "$sessionBaseUrl/connection/$connectionId"
    private val streamBaseUrl = "$sessionBaseUrl/stream"
    private val streamUrl = "$streamBaseUrl/$streamId"
    private val archiveBaseUrl = "$sessionBaseUrl/archive/$archiveId"
    private val broadcastBaseUrl = "$sessionBaseUrl/broadcast/$broadcastId"
    private val renderBaseUrl = "$sessionBaseUrl/render/$renderId"
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
    private val videoType = VideoType.CAMERA
    private val streamName = "My Stream"
    private val layoutClasses = listOf("full", "no-border")
    private val streamLayoutMap = mapOf(
        "id" to streamId,
        "videoType" to videoType.name.lowercase(),
        "name" to streamName,
        "layoutClassList" to layoutClasses
    )

    private fun assertEqualsSampleStream(response: GetStreamResponse) {
        assertNotNull(response)
        assertEquals(UUID.fromString(streamId), response.id)
        assertEquals(videoType, response.videoType)
        assertEquals(streamName, response.name)
        assertEquals(layoutClasses, response.layoutClassList)
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
        val maxDuration = 1800
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
        mockPost(expectedUrl = "$sessionBaseUrl/play-dtmf", expectedRequestParams = mapOf("digits" to dtmf))
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
        mockPost(expectedUrl = "$sessionBaseUrl/signal", expectedRequestParams = signalRequestMap, status = 204)
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
        mockPost(expectedUrl = "$sessionBaseUrl/mute",
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

        mockPost(expectedUrl = "$sessionBaseUrl/mute",
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
        val count = 4
        mockGet(expectedUrl = streamBaseUrl, expectedResponseParams = mapOf(
            "count" to count,
            "items" to listOf(
                mapOf(),
                streamLayoutMap,
                mapOf("id" to randomUuidStr),
                mapOf("layoutClassList" to listOf<String>())
            )
        ))
        val response = existingSession.listStreams()
        assertEquals(count, response.size)
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
            authType = AuthType.JWT,
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
        val mediaServerUrl = "$exampleUrlBase/media"
        val location = "127.0.0.1"
        val archiveMode = ArchiveMode.ALWAYS

        mockPostQueryParams(
            expectedUrl = "/session/create",
            authType = AuthType.JWT,
            expectedRequestParams = mapOf(
                "archiveMode" to archiveMode.name.lowercase(),
                "location" to location,
                "p2p.preference" to "disabled"
            ),
            expectedResponseParams = listOf(mapOf(
                "session_id" to sessionId,
                "application_id" to applicationId,
                "create_dt" to createdAtLong,
                "media_server_url" to mediaServerUrl
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
        assertEquals(URI.create(mediaServerUrl), response.mediaServerUrl)
    }
}