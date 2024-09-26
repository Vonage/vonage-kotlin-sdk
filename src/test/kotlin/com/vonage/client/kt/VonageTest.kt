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

import com.vonage.client.HttpWrapper
import com.vonage.client.VonageClient
import org.apache.commons.lang3.reflect.FieldUtils
import kotlin.test.*

class VonageTest {

    @Test
    fun `live testing placeholder`() {
        val client = Vonage { authFromEnv(); signatureSecret(null) }
        println("Finished") // Place debug breakpoint here
    }

    @Test
    fun `test user agent is not overriden`() {
        val timeout = 36000
        val customUa = "MyCustomUserAgent"
        val client = Vonage { httpConfig { timeoutMillis(timeout); appendUserAgent(customUa) } }
        val wrapper = FieldUtils.readField(
            client::class.java.getDeclaredField("client").apply { isAccessible = true }.get(client),
            "httpWrapper", true
        ) as HttpWrapper

        assertEquals(
            "vonage-kotlin-sdk/$VONAGE_KOTLIN_SDK_VERSION",
            wrapper.httpConfig.customUserAgent
        )
        assertEquals(timeout, wrapper.httpConfig.timeoutMillis)
    }
}