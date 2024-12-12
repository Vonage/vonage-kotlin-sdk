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
import org.apache.commons.lang3.reflect.FieldUtils
import kotlin.test.*

class VonageTest {

    @Test
    fun `live testing placeholder`() {
        val client = Vonage { authFromEnv(); signatureSecret(null) }
        println("Finished") // Place debug breakpoint here
    }

    @Test
    fun `Ensure that custom user agent does not override SDK's default prefix`() {
        val expectedDefaultUa = "vonage-kotlin-sdk/$VONAGE_KOTLIN_SDK_VERSION"
        var client = Vonage { }
        assertEquals(expectedDefaultUa, client.getWrapper().httpConfig.customUserAgent)

        val timeout = 36000
        val customUa = "My_Custom User-Agent"
        client = Vonage { httpConfig { timeoutMillis(timeout); appendUserAgent(customUa) } }
        assertEquals(timeout, client.getWrapper().httpConfig.timeoutMillis)
        assertEquals("$expectedDefaultUa $customUa", client.getWrapper().httpConfig.customUserAgent)
    }

    private fun Vonage.getWrapper(): HttpWrapper {
        return FieldUtils.readField(
            this::class.java.getDeclaredField("client").apply { isAccessible = true }.get(this),
            "httpWrapper", true
        ) as HttpWrapper
    }
}