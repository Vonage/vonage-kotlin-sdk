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

/**
 * Implementation of the [Users API](https://developer.vonage.com/en/api/application.v2#User).
 *
 * *Authentication method:* JWT.
 */
class Users internal constructor(private val client: UsersClient) {

    /**
     * Call this method to work with an existing user.
     *
     * @param userId The UUID of the user to work with.
     *
     * @return An [ExistingUser] object with methods to interact with the user.
     */
    fun user(userId: String): ExistingUser = ExistingUser(userId)

    /**
     * Call this method to work with an existing user.
     *
     * @param user The user object to work with.
     *
     * @return An [ExistingUser] object with methods to interact with the user.
     */
    fun user(user: BaseUser): ExistingUser = ExistingUser(user.id)

    /**
     * Class for working with an existing user.
     *
     * @property id The user ID.
     */
    inner class ExistingUser internal constructor(id: String): ExistingResource(id) {

        /**
         * Retrieves the user details.
         *
         * @return The user details.
         *
         * @throws [UsersResponseException] If the user details cannot be retrieved.
         */
        fun get(): User = client.getUser(id)

        /**
         * Updates the user details.
         *
         * @param properties A lambda function to set the properties of the user.
         *
         * @return The updated user details.
         *
         * @throws [UsersResponseException] If the user details cannot be updated.
         */
        fun update(properties: User.Builder.() -> Unit): User =
            client.updateUser(id, User.builder().apply(properties).build())

        /**
         * Deletes the user.
         *
         * @throws [UsersResponseException] If the user cannot be deleted.
         */
        fun delete(): Unit = client.deleteUser(id)
    }

    /**
     * Creates a new user.
     *
     * @param properties A lambda function to set the properties of the user.
     *
     * @return The created user details.
     *
     * @throws [UsersResponseException] If the user cannot be created.
     */
    fun create(properties: User.Builder.() -> Unit): User =
        client.createUser(User.builder().apply(properties).build())

    /**
     * List users in your application. The response will only contain a subset of the user's properties.
     * Call the [user] method to get the full details of a particular user.
     *
     * @param filter (OPTIONAL) A lambda function to set the request properties for pagination.
     * If omitted, the maximum number of users is returned; which is the first 100.
     *
     * @return The HAL response page with a list of basic user details (ID and name).
     */
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
