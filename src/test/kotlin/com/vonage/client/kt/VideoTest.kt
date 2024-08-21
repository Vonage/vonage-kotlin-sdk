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

import java.util.*
import kotlin.test.*

class VideoTest : AbstractTest() {
    private val verifyClient = vonage.verify
    private val baseUrl = "/v2/project/$applicationId"
    private val sessionId = "flR1ZSBPY3QgMjkgMTI6MTM6MjMgUERUIDIwMTN"
    private val connectionId = testUuidStr
    private val streamId = "8b732909-0a06-46a2-8ea8-074e64d43422"
    private val archiveId = "b40ef09b-3811-4726-b508-e41a0f96c68f"
    private val broadcastId = "93e36bb9-b72c-45b6-a9ea-5c37dbc49906"
    private val captionsId = "7c0680fc-6274-4de5-a66f-d0648e8d3ac2"
    private val audioConnectorId = "b0a5a8c7-dc38-459f-a48d-a7f2008da853"
    private val jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2OTkwNDMxMTEsImV4cCI6MTY5OTA2NDcxMSwianRpIjoiMW1pODlqRk9meVpRIiwiYXBwbGljYXRpb25faWQiOiIxMjMxMjMxMi0zODExLTQ3MjYtYjUwOC1lNDFhMGY5NmM2OGYiLCJzdWIiOiJ2aWRlbyIsImFjbCI6IiJ9.o3U506EejsS8D5Tob90FG1NC1cR69fh3pFOpxnyTHVFfgqI6NWuuN8lEwrS3Zb8bGxE_A9LyyUZ2y4uqLpyXRw"
    private val createdAtLong = 1414642898000L
    private val sessionBaseUrl = "$baseUrl/session/$sessionId"

}