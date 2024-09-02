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

import com.vonage.client.users.*

class Users internal constructor(private val client: UsersClient) {

    fun user(userId: String): ExistingUser = ExistingUser(userId)

    fun user(user: BaseUser): ExistingUser = ExistingUser(user.id)

    inner class ExistingUser internal constructor(id: String): ExistingResource(id) {
        fun get(): User = client.getUser(id)

        fun update(properties: User.Builder.() -> Unit): User =
            client.updateUser(id, User.builder().apply(properties).build())

        fun delete(): Unit = client.deleteUser(id)
    }

    fun create(properties: User.Builder.() -> Unit): User =
        client.createUser(User.builder().apply(properties).build())

    fun list(filter: (ListUsersRequest.Builder.() -> Unit)? = null): ListUsersResponse {
        val request = ListUsersRequest.builder()
        if (filter == null) {
            request.pageSize(100)
        }
        else {
            request.apply(filter)
        }
        return client.listUsers(request.build())
    }
}
