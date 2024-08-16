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

import com.vonage.client.common.HttpMethod
import com.vonage.client.users.*
import java.net.URI
import kotlin.test.*

class UsersTest : AbstractTest() {
    private val client = vonage.users
    private val authType = AuthType.JWT
    private val baseUrl = "/v1/users"
    private val userId = "USR-82e028d9-5201-4f1e-8188-604b2d3471ec"
    private val userUrl = "$baseUrl/$userId"
    private val existingUser = client.user(userId)
    private val name = "my_user_name"
    private val displayName = "Test User"
    private val ttl = 3600
    private val pageSize = 10
    private val customData = mapOf("custom_key" to "custom_value")
    private val sipUser = "sip_user"
    private val sipPassword = "Passw0rd1234"
    private val messengerId = "12345abcd"
    private val baseUserRequest = mapOf("name" to name)
    private val userIdMapOnly = mapOf("id" to userId)
    private val baseUserResponse = userIdMapOnly + baseUserRequest
    private val fullUserRequest = baseUserRequest + mapOf(
        "display_name" to displayName,
        "image_url" to imageUrl,
        "properties" to mapOf(
            "custom_data" to customData,
            "ttl" to ttl
        ),
        "custom_data" to customData,
        "channels" to mapOf(
            "sip" to listOf(mapOf(
                "uri" to sipUri,
                "username" to sipUser,
                "password" to sipPassword
            )),
            "messenger" to listOf(mapOf(
                "id" to messengerId
            ))
        )
    )
    private val fullUserResponse = userIdMapOnly + fullUserRequest

    private fun assertEqualsUser(parsed: User) {
        assertEquals(userId, parsed.id)
        assertEquals(name, parsed.name)
        assertEquals(displayName, parsed.displayName)
        assertEquals(URI.create(imageUrl), parsed.imageUrl)
        assertEquals(customData, parsed.customData)
        val channels = parsed.channels
        assertNotNull(channels)
        // TODO the rest
    }

    private fun assertUserNotFoundException(method: HttpMethod, invocation: Users.ExistingUser.() -> Any) =
        assertApiResponseException<UsersResponseException>(
            url = userUrl, requestMethod = method,
            actualCall = { invocation.invoke(existingUser) },
            status = 404,
            title = "Not found.",
            errorType = "https://developer.vonage.com/api/conversation#user:error:not-found",
            code = "user:error:not-found",
            detail = "User does not exist, or you do not have access.",
            instance = "00a5916655d650e920ccf0daf40ef4ee"
        )

    private fun assertListUsers(filter: Map<String, Any>, invocation: Users.() -> ListUsersResponse) {
        mockGet(
            expectedUrl = baseUrl, authType = authType,
            expectedQueryParams = filter,
            expectedResponseParams = mapOf(
                "page_size" to pageSize,
                "_embedded" to mapOf(
                    "users" to listOf(
                        mapOf(),
                        mapOf(
                            "id" to userId,
                            "name" to name,
                            "display_name" to displayName,
                            "_links" to mapOf(
                                "self" to mapOf(
                                    "href" to "https://api.nexmo.com$userUrl"
                                )
                            )
                        ),
                        userIdMapOnly
                    )
                ),
                "_links" to mapOf(
                    "first" to mapOf(
                        "href" to "https://api.nexmo.com/v1/users?order=desc&page_size=10"
                    ),
                    "self" to mapOf(
                        "href" to "https://api.nexmo.com/v1/users?order=desc&page_size=10&cursor=7EjDNQrAcipmOnc0HCzpQRkhBULzY44ljGUX4lXKyUIVfiZay5pv9wg%3D"
                    ),
                    "next" to mapOf(
                        "href" to "https://api.nexmo.com/v1/users?order=desc&page_size=10&cursor=7EjDNQrAcipmOnc0HCzpQRkhBULzY44ljGUX4lXKyUIVfiZay5pv9wg%3D"
                    ),
                    "prev" to mapOf(
                        "href" to "https://api.nexmo.com/v1/users?order=desc&page_size=10&cursor=7EjDNQrAcipmOnc0HCzpQRkhBULzY44ljGUX4lXKyUIVfiZay5pv9wg%3D"
                    )
                )
            )
        )
        val response = invocation.invoke(client)
        assertNotNull(response)
        val users = response.users
        assertNotNull(users)
        assertEquals(3, users.size)
        // TODO remaining assertions
    }

    @Test
    fun `get user`() {
        mockGet(expectedUrl = userUrl, authType = authType, expectedResponseParams = fullUserResponse)
        assertEqualsUser(existingUser.get())
        assertUserNotFoundException(HttpMethod.GET, Users.ExistingUser::get)
    }

    @Test
    fun `delete user`() {
        mockDelete(expectedUrl = userUrl, authType = authType)
        existingUser.delete()
        assertUserNotFoundException(HttpMethod.DELETE, Users.ExistingUser::delete)
    }

    @Test
    fun `update user`() {
        val newDisplayName = "Updated DP"
        val newPic = "$exampleUrlBase/new_pic.png"
        mockPatch(
            expectedUrl = userUrl, authType = authType,
            expectedRequestParams = userIdMapOnly + mapOf(
                "display_name" to newDisplayName,
                "image_url" to newPic
            ),
            expectedResponseParams = fullUserResponse
        )
        assertEqualsUser(existingUser.update {
            displayName(newDisplayName)
            imageUrl(newPic)
        })

        assertUserNotFoundException(HttpMethod.PATCH) { update {  }}
    }

    @Test
    fun `create user required parameters`() {
        mockPost(
            expectedUrl = baseUrl, authType = authType,
            expectedRequestParams = mapOf(),
            expectedResponseParams = fullUserResponse
        )
        assertEqualsUser(client.create {})

        assert401ApiResponseException<UsersResponseException>(baseUrl, HttpMethod.POST) {
            client.create {}
        }
    }

    @Test
    fun `create user all parameters`() {
        mockPost(
            expectedUrl = baseUrl, authType = authType,
            expectedRequestParams = fullUserRequest,
            expectedResponseParams = fullUserResponse
        )
        assertEqualsUser(client.create {
            name(name)
            displayName(displayName)
            imageUrl(imageUrl)
            customData(customData)
            // TODO channels
        })
    }

    @Test
    fun `list users no filter`() {
        assertListUsers(emptyMap(), Users::list)
        assert401ApiResponseException<UsersResponseException>(baseUrl, HttpMethod.GET, client::list)
    }

    @Test
    fun `list users all filters`() {
        assertListUsers(mapOf(
            "order" to "desc",
            "page_size" to pageSize
            // TODO remaining filters
        )) {
            list {
                order(ListUsersRequest.SortOrder.DESC)
                pageSize(pageSize)
            }
        }
    }
}