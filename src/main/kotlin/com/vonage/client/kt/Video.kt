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

    fun session(sessionId: String): ExistingSession = ExistingSession(sessionId)

    inner class ExistingSession internal constructor(val sessionId: String) {

    }

    fun archive(archiveId: String): ExistingArchive = ExistingArchive(archiveId)

    inner class ExistingArchive internal constructor(val archiveId: String) {

    }

    fun broadcast(broadcastId: String): ExistingBroadcast = ExistingBroadcast(broadcastId)

    inner class ExistingBroadcast internal constructor(val broadcastId: String) {

    }

    fun experienceComposer(renderId: String): ExistingRender = ExistingRender(renderId)

    inner class ExistingRender internal constructor(val renderId: String) {

    }
}
