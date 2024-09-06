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
import com.vonage.client.users.channels.*
import java.net.URI
import kotlin.test.*

class UsersTest : AbstractTest() {
    private val client = vonage.users
    private val authType = AuthType.JWT
    private val baseUrl = "/v1/users"
    private val userId = "USR-82e028d9-5201-4f1e-8188-604b2d3471ec"
    private val userUrl = "$baseUrl/$userId"
    private val existingUser = client.user(userId)
    private val displayName = "Test User"
    private val ttl = 3600 // TODO support
    private val pageSize = 10
    private val customData = mapOf("custom_key" to "custom_value")
    private val sipUser = "sip_user"
    private val sipPassword = "Passw0rd1234"
    private val messengerId = "12345abcd"
    private val href = "https://api.nexmo.com/users"
    private val order = "desc"
    private val firstHref = "$href?order=$order&page_size=$pageSize"
    private val navUrl = "$firstHref&cursor=$cursor"
    private val userHref = "https://api.nexmo.com$userUrl"
    private val navUrlMap = mapOf("href" to navUrl)
    private val baseUserRequest = mapOf("name" to userName)
    private val userIdMapOnly = mapOf("id" to userId)
    private val displayNameMap = mapOf("display_name" to displayName)
    private val fullUserRequest = baseUserRequest + displayNameMap + mapOf(
        "image_url" to imageUrl,
        "channels" to mapOf(
            "pstn" to listOf(mapOf(
                "number" to toNumber
            )),
            "sip" to listOf(mapOf(
                "uri" to sipUri,
                "username" to sipUser,
                "password" to sipPassword
            )),
            "vbc" to listOf(mapOf(
                "extension" to vbcExt
            )),
            "websocket" to listOf(mapOf(
                "uri" to websocketUri,
                "content-type" to wsContentTypeStr,
                "headers" to customData
            )),
            "mms" to listOf(mapOf(
                "number" to toNumber
            )),
            "whatsapp" to listOf(mapOf(
                "number" to altNumber
            )),
            "viber" to listOf(mapOf(
                "number" to altNumber
            )),
            "messenger" to listOf(mapOf(
                "id" to messengerId
            ))
        ),
        "properties" to mapOf(
            "custom_data" to customData
        )
    )
    private val fullUserResponse = userIdMapOnly + fullUserRequest

    private fun assertEqualsUser(parsed: User) {
        assertEquals(userId, parsed.id)
        assertEquals(userName, parsed.name)
        assertEquals(displayName, parsed.displayName)
        assertEquals(URI.create(imageUrl), parsed.imageUrl)
        assertEquals(customData, parsed.customData)
        val channels = parsed.channels
        assertNotNull(channels)

        val pstn = channels.pstn
        assertNotNull(pstn)
        assertEquals(1, pstn.size)
        assertEquals(toNumber, pstn[0].number)

        val mms = channels.mms
        assertNotNull(mms)
        assertEquals(1, mms.size)
        assertEquals(toNumber, mms[0].number)

        val whatsapp = channels.whatsapp
        assertNotNull(whatsapp)
        assertEquals(1, whatsapp.size)
        assertEquals(altNumber, whatsapp[0].number)

        val viber = channels.viber
        assertNotNull(viber)
        assertEquals(1, viber.size)
        assertEquals(altNumber, viber[0].number)

        val messenger = channels.messenger
        assertNotNull(messenger)
        assertEquals(1, messenger.size)
        assertEquals(messengerId, messenger[0].id)

        val websocket = channels.websocket
        assertNotNull(websocket)
        assertEquals(1, websocket.size)
        assertEquals(URI.create(websocketUri), websocket[0].uri)
        assertEquals(Websocket.ContentType.fromString(wsContentTypeStr), websocket[0].contentType)
        assertEquals(customData, websocket[0].headers)

        val sip = channels.sip
        assertNotNull(sip)
        assertEquals(1, sip.size)
        assertEquals(URI.create(sipUri), sip[0].uri)
        assertEquals(sipUser, sip[0].username)
        assertEquals(sipPassword, sip[0].password)

        val vbc = channels.vbc
        assertNotNull(vbc)
        assertEquals(1, vbc.size)
        assertEquals(vbcExt, vbc[0].extension)
    }

    private fun assertUserNotFoundException(method: HttpMethod, invocation: Users.ExistingUser.() -> Any) =
        assertApiResponseException<UsersResponseException>(
            url = userUrl, requestMethod = method,
            actualCall = { invocation(existingUser) },
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
                        userIdMapOnly + baseUserRequest + displayNameMap + mapOf(
                            "_links" to mapOf(
                                "self" to mapOf("href" to userHref)
                            )
                        ),
                        userIdMapOnly
                    )
                ),
                "_links" to mapOf(
                    "first" to mapOf("href" to firstHref),
                    "self" to navUrlMap,
                    "next" to navUrlMap,
                    "prev" to navUrlMap
                )
            )
        )
        val response = invocation(client)
        assertNotNull(response)
        val users = response.users
        assertNotNull(users)
        assertEquals(3, users.size)
        val emptyUser = users[0]
        assertNull(emptyUser.id)
        assertNull(emptyUser.name)
        val mainUser = users[1]
        assertEquals(userId, mainUser.id)
        assertEquals(userName, mainUser.name)
        val idOnlyUser = users[2]
        assertEquals(userId, idOnlyUser.id)
        assertNull(idOnlyUser.name)
        val links = response.links
        assertNotNull(links)
        assertEquals(URI.create(firstHref), links.firstUrl)
        assertEquals(URI.create(navUrl), links.selfUrl)
        assertEquals(URI.create(navUrl), links.nextUrl)
        assertEquals(URI.create(navUrl), links.prevUrl)

        assertEquals(existingUser.id, client.user(idOnlyUser).id)

        assert401ApiResponseException<UsersResponseException>(baseUrl, HttpMethod.GET) {
            invocation(client)
        }
    }

    @Test
    fun `existing user equals and hashCode`() {
        val differentUser = client.user("USR-$testUuidStr")
        assertEquals(existingUser, existingUser)
        assertFalse(existingUser.equals(userId))
        assertEquals(userId.hashCode(), existingUser.hashCode())
        assertEquals(existingUser, client.user(userId))
        assertNotEquals(existingUser, differentUser)
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
    fun `create user all parameters and channels`() {
        mockPost(
            expectedUrl = baseUrl, authType = authType,
            expectedRequestParams = fullUserRequest,
            expectedResponseParams = fullUserResponse
        )
        assertEqualsUser(client.create {
            name(userName)
            displayName(displayName)
            imageUrl(imageUrl)
            customData(customData)
            channels(
                Pstn(toNumber), Sip(sipUri, sipUser, sipPassword),
                Websocket(websocketUri, Websocket.ContentType.fromString(wsContentTypeStr), customData),
                Vbc(vbcExt.toInt()), Mms(toNumber), Whatsapp(altNumber),
                Viber(altNumber), Messenger(messengerId)
            )
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
            "page_size" to pageSize,
            "order" to "desc",
            "cursor" to cursor,
            "name" to userName
        )) {
            list {
                order(ListUsersRequest.SortOrder.DESC); name(userName)
                pageSize(pageSize); cursor(URI.create(navUrl))
            }
        }
    }
}