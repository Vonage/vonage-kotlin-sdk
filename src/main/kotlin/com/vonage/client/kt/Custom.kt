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

import com.vonage.client.*
import com.vonage.client.common.HttpMethod

/**
 * Custom client for making HTTP requests to APIs that are unsupported by this SDK.
 * This will automatically handle authentication and (de)serialisation for you.
 *
 * Requests should be JSON-based; either a Map representation of the structure or an implementation of [Jsonable].
 * Responses (i.e. the `R` parameter) may be one of the following:
 * - `Map<String, *>` representation of the JSON response.
 * - `Collection<*>` representation of the JSON response.
 * - `String` representation of the response body.
 * - A custom object that implements the [Jsonable] interface to parse the JSON response into.
 * - [ByteArray] for binary response bodies.
 * - [Void] for empty response bodies.
 *
 * Please note that you must ALWAYS explicitly provide the type parameters when calling methods on this client and
 * also provide the type in the response assignment, otherwise the compiler won't be able to infer the correct type.
 *
 * @param internalJavaSDKCustomClient The underlying Java SDK implementation which this client delegates to.
 *
 * @since 2.1.0
 */
class Custom internal constructor(val internalJavaSDKCustomClient: CustomClient) {

    /**
     * Advanced method for making requests to APIs that are unsupported by this SDK. This is the most flexible option.
     *
     * @param requestMethod The HTTP method to use for the request as an enum.
     * @param url Absolute URL to send the request to as a string.
     * @param body The request body, typically in JSON format. See [DynamicEndpoint.makeRequest] for acceptable types.
     *
     * @return The response body, which can be a Map, Collection, String, or custom object implementing [Jsonable].
     * @throws VonageApiResponseException If the HTTP response code is 400 or greater.
     */
    inline fun <reified T, reified R> makeRequest(requestMethod: HttpMethod, url: String, body: T): R =
        internalJavaSDKCustomClient.makeRequest<T, R>(requestMethod, url, body)

    /**
     * Sends a `DELETE` request to the specified URL.
     *
     * @param url Absolute URL to send the request to as a string.
     *
     * @return The response body if present, typically as JSON. See the class documentation for acceptable types.
     * @throws VonageApiResponseException If the HTTP response code is 400 or greater.
     */
    inline fun <reified R> delete(url: String): R =
        internalJavaSDKCustomClient.delete<R>(url)

    /**
     * Sends a `GET` request to the specified URL.
     *
     * @param url Absolute URL to send the request to as a string.
     *
     * @return The response body if present, typically as JSON. See the class documentation for acceptable types.
     * @throws VonageApiResponseException If the HTTP response code is 400 or greater.
     */
    inline fun <reified R> get(url: String): R =
        internalJavaSDKCustomClient.get<R>(url)

    /**
     * Sends a `POST` request to the specified URL with a JSON body.
     *
     * @param url Absolute URL to send the request to as a string.
     * @param body The request body as a [Jsonable] object.
     *
     * @return The response body if present, typically as JSON. See the class documentation for acceptable types.
     * @throws VonageApiResponseException If the HTTP response code is 400 or greater.
     */
    inline fun <reified R> post(url: String, body: Jsonable): R =
        internalJavaSDKCustomClient.post<R>(url, body)

    /**
     * Sends a `POST` request to the specified URL with a JSON body.
     *
     * @param url Absolute URL to send the request to as a string.
     * @param body The request body in JSON format as a Map tree structure.
     *
     * @return The response body if present, typically as JSON. See the class documentation for acceptable types.
     * @throws VonageApiResponseException If the HTTP response code is 400 or greater.
     */
    inline fun <reified R> post(url: String, body: Map<String, *>): R =
        internalJavaSDKCustomClient.post<R>(url, body)

    /**
     * Sends a `PUT` request to the specified URL with a JSON body.
     *
     * @param url Absolute URL to send the request to as a string.
     * @param body The request body as a [Jsonable] object.
     *
     * @return The response body if present, typically as JSON. See the class documentation for acceptable types.
     * @throws VonageApiResponseException If the HTTP response code is 400 or greater.
     */
    inline fun <reified R> put(url: String, body: Jsonable): R =
        internalJavaSDKCustomClient.put<R>(url, body)

    /**
     * Sends a `PUT` request to the specified URL with a JSON body.
     *
     * @param url Absolute URL to send the request to as a string.
     * @param body The request body in JSON format as a Map tree structure.
     *
     * @return The response body if present, typically as JSON. See the class documentation for acceptable types.
     * @throws VonageApiResponseException If the HTTP response code is 400 or greater.
     */
    inline fun <reified R> put(url: String, body: Map<String, *>): R =
        internalJavaSDKCustomClient.put<R>(url, body)

    /**
     * Sends a `PATCH` request to the specified URL with a JSON body.
     *
     * @param url Absolute URL to send the request to as a string.
     * @param body The request body as a [Jsonable] object.
     *
     * @return The response body if present, typically as JSON. See the class documentation for acceptable types.
     * @throws VonageApiResponseException If the HTTP response code is 400 or greater.
     */
    inline fun <reified R> patch(url: String, body: Jsonable): R =
        internalJavaSDKCustomClient.patch<R>(url, body)

    /**
     * Sends a `PATCH` request to the specified URL with a JSON body.
     *
     * @param url Absolute URL to send the request to as a string.
     * @param body The request body in JSON format as a Map tree structure.
     *
     * @return The response body if present, typically as JSON. See the class documentation for acceptable types.
     * @throws VonageApiResponseException If the HTTP response code is 400 or greater.
     */
    inline fun <reified R> patch(url: String, body: Map<String, *>): R =
        internalJavaSDKCustomClient.patch<R>(url, body)
}
