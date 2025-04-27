/*
 *   Copyright 2025 Vonage
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

import com.fasterxml.jackson.databind.ObjectMapper
import com.vonage.client.Jsonable
import com.vonage.client.account.BalanceResponse
import com.vonage.client.common.HttpMethod
import kotlin.test.*

class CustomTest : AbstractTest() {
    private val client = vonage.custom
    private val expectedUrl = "/unsupported"
    private val absoluteUrl = wmBaseUrl + expectedUrl
    private val bodyMap = mapOf(
        "Hello" to "World",
        "A" to 1,
        "B" to listOf(2, 3),
        "C" to true,
        "d4" to mapOf(
            "e5" to "f6",
            "g7" to listOf("h8", "i9"),
            "j10" to mapOf(
                "k11" to 3.1415926,
                "l13" to true,
                "m14" to listOf(15, 16)
            )
        )
    )
    private val jsonableMap = mapOf("value" to 10.28, "autoReload" to false)
    private val jsonableObj = Jsonable.fromJson<BalanceResponse>("{\"value\":10.28,\"autoReload\":false}")

    private fun assertEqualsJsonableObject(response: BalanceResponse) {
        assertNotNull(response)
        assertEquals(10.28, response.value)
        assertFalse(response.isAutoReload)
    }

    private fun mockGet() =
        mockRequest(HttpMethod.GET, expectedUrl, authType = AuthType.JWT)
            .mockReturn(200, bodyMap)

    private fun mockPost() =
        mockRequest(HttpMethod.POST, expectedUrl, authType = AuthType.JWT)
            .mockReturn(200, bodyMap)

    private fun mockPut() =
        mockRequest(HttpMethod.PUT, expectedUrl, authType = AuthType.JWT)
            .mockReturn(200, bodyMap)

    private fun mockPatch() =
        mockRequest(HttpMethod.PATCH, expectedUrl, authType = AuthType.JWT)
            .mockReturn(200, bodyMap)

    @Test
    fun `make request DELETE with Jsonable body`() {
        val request = mockRequest(HttpMethod.DELETE, expectedUrl,
            authType = AuthType.JWT,
            contentType = ContentType.APPLICATION_JSON,
            expectedParams = jsonableMap
        )

        request.mockReturn(200, bodyMap)
        val response = client.makeRequest<Jsonable, Map<String, *>>(
            HttpMethod.DELETE, absoluteUrl, jsonableObj
        )
        assertEquals(bodyMap, response)

        request.mockReturn(200, jsonableMap)
        assertEqualsJsonableObject(client.makeRequest<Jsonable, BalanceResponse>(
            HttpMethod.DELETE, absoluteUrl, jsonableObj
        ))
    }

    @Test
    fun `delete no response`() {
        mockDelete(expectedUrl)
        client.delete<Void>(absoluteUrl)
    }

    @Test
    fun `get String response`() {
        mockGet()
        val response: String = client.get(absoluteUrl)
        assertEquals(ObjectMapper().writeValueAsString(bodyMap), response)
    }

    @Test
    fun `get Map response`() {
        mockGet()
        val response: Map<String, *> = client.get(absoluteUrl)
        assertEquals(bodyMap, response)
    }

    @Test
    fun `get Jsonable response`() {
        mockGet(expectedUrl, expectedResponseParams = jsonableMap)
        assertEqualsJsonableObject(client.get(absoluteUrl))
    }

    @Test
    fun `post no response`() {
        mockPost(expectedUrl)
        client.post<Void>(absoluteUrl, bodyMap)
        client.post<Void>(absoluteUrl, jsonableObj)
        client.post<Void>(absoluteUrl, jsonableMap)
    }

    @Test
    fun `post Jsonable response`() {
        mockPost(expectedUrl, expectedResponseParams = jsonableMap)
        assertEqualsJsonableObject(client.post(absoluteUrl, jsonableObj))
        assertEqualsJsonableObject(client.post(absoluteUrl, jsonableMap))
    }

    @Test
    fun `post Map response`() {
        mockPost()
        val response: Map<String, *> = client.post(absoluteUrl, bodyMap)
        assertEquals(bodyMap, response)
    }

    @Test
    fun `post String response`() {
        mockPost()
        val response: String = client.post(absoluteUrl, bodyMap)
        assertEquals(ObjectMapper().writeValueAsString(bodyMap), response)
    }

    @Test
    fun `put Jsonable response`() {
        mockPut(expectedUrl, expectedResponseParams = jsonableMap)
        assertEqualsJsonableObject(client.put(absoluteUrl, jsonableObj))
        assertEqualsJsonableObject(client.put(absoluteUrl, jsonableMap))
    }

    @Test
    fun `put Map response`() {
        mockPut()
        val response: Map<String, *> = client.put(absoluteUrl, bodyMap)
        assertEquals(bodyMap, response)
    }

    @Test
    fun `put String response`() {
        mockPut()
        val response: String = client.put(absoluteUrl, bodyMap)
        assertEquals(ObjectMapper().writeValueAsString(bodyMap), response)
    }

    @Test
    fun `patch Jsonable response`() {
        mockPatch(expectedUrl, expectedResponseParams = jsonableMap)
        assertEqualsJsonableObject(client.patch(absoluteUrl, jsonableObj))
        assertEqualsJsonableObject(client.patch(absoluteUrl, jsonableMap))
    }

    @Test
    fun `patch Map response`() {
        mockPatch()
        val response: Map<String, *> = client.patch(absoluteUrl, bodyMap)
        assertEquals(bodyMap, response)
    }

    @Test
    fun `patch String response`() {
        mockPatch()
        val response: String = client.patch(absoluteUrl, bodyMap)
        assertEquals(ObjectMapper().writeValueAsString(bodyMap), response)
    }
}