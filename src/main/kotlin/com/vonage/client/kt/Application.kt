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

import com.vonage.client.application.*
import com.vonage.client.application.Application
import com.vonage.client.application.capabilities.*
import com.vonage.client.application.capabilities.Messages
import com.vonage.client.application.capabilities.Verify
import com.vonage.client.application.capabilities.Voice
import com.vonage.client.common.Webhook
import java.util.*

/**
 * Implementation of the [Application API](https://developer.vonage.com/en/api/application.v2).
 */
class Application internal constructor(private val client: ApplicationClient) {

    /**
     * Call this method to work with an existing application.
     *
     * @param applicationId The UUID of the application to work with.
     *
     * @return An [ExistingApplication] object with methods to interact with the application.
     */
    fun application(applicationId: String): ExistingApplication = ExistingApplication(applicationId)


    /**
     * Call this method to work with an existing application.
     *
     * @param applicationId The UUID of the application to work with.
     *
     * @return An [ExistingApplication] object with methods to interact with the application.
     */
    fun application(applicationId: UUID) = application(applicationId.toString())

    /**
     * Class for working with an existing application.
     *
     * @property id The application ID.
     */
    inner class ExistingApplication internal constructor(id: String): ExistingResource(id) {

        /**
         * Retrieves the application details.
         *
         * @return The application details.
         *
         * @throws [ApplicationResponseException] If the application details could not be retrieved.
         */
        fun get(): Application = client.getApplication(id)

        /**
         * Updates the application details.
         *
         * @param properties A lambda function that takes an [Application.Builder] object as its receiver.
         * Use this to specify properties to update on the application.
         *
         * @return The updated application details.
         *
         * @throws [ApplicationResponseException] If the application details could not be updated.
         */
        fun update(properties: Application.Builder.() -> Unit): Application =
            client.updateApplication(Application.builder(get()).apply(properties).build())

        /**
         * Deletes the application.
         *
         * @throws [ApplicationResponseException] If the application could not be deleted.
         */
        fun delete(): Unit = client.deleteApplication(id)
    }

    /**
     * Retrieves a list of all applications associated with your Vonage account (up to the first 1000).
     *
     * @return A list containing all application details (maximum 1000).
     *
     * @throws [ApplicationResponseException] If the list of applications could not be retrieved.
     */
    fun listAll(): List<Application> = client.listAllApplications()

    /**
     * Retrieves a list of applications associated with your Vonage account.
     *
     * @param page (OPTIONAL) The page number to retrieve.
     * @param pageSize (OPTIONAL) The maximum number of applications per page.
     *
     * @return A list containing application details.
     *
     * @throws [ApplicationResponseException] If the list of applications could not be retrieved.
     */
    fun list(page: Int? = null, pageSize: Int? = null): List<Application> {
        val filter = ListApplicationRequest.builder()
        if (page != null) filter.page(page.toLong())
        if (pageSize != null) filter.pageSize(pageSize.toLong())
        return client.listApplications(filter.build()).applications
    }

    /**
     * Creates a new application.
     *
     * @param properties A lambda function that takes an [Application.Builder] object as its receiver.
     * Use this to specify properties for the new application.
     *
     * @return The newly created application details.
     *
     * @throws [ApplicationResponseException] If the application could not be created.
     */
    fun create(properties: Application.Builder.() -> Unit): Application =
        client.createApplication(Application.builder().apply(properties).build())
}

private fun webhookBuilder(properties: Webhook.Builder.() -> Unit): Webhook =
    Webhook.builder().apply(properties).build()

/**
 * Adds a webhook to the application. This is an alias for [Webhook.Builder.address].
 *
 * @param url The URL to send the webhook to as a string.
 *
 * @return The webhook builder.
 */
fun Webhook.Builder.url(url: String): Webhook.Builder = address(url)

/**
 * Adds an `answer_url` webhook to the [Voice] capability.
 *
 * @param properties A lambda function for setting additional properties on the webhook.
 *
 * @return The Voice capability builder.
 */
fun Voice.Builder.answer(properties: Webhook.Builder.() -> Unit): Voice.Builder =
    addWebhook(Webhook.Type.ANSWER, webhookBuilder(properties))

/**
 * Adds an `fallback_answer_url` webhook to the [Voice] capability.
 *
 * @param properties A lambda function for setting additional properties on the webhook.
 *
 * @return The Voice capability builder.
 */
fun Voice.Builder.fallbackAnswer(properties: Webhook.Builder.() -> Unit): Voice.Builder =
    addWebhook(Webhook.Type.FALLBACK_ANSWER, webhookBuilder(properties))

/**
 * Adds an `event_url` webhook to the [Voice] capability.
 *
 * @param properties A lambda function for setting additional properties on the webhook.
 *
 * @return The Voice capability builder.
 */
fun Voice.Builder.event(properties: Webhook.Builder.() -> Unit): Voice.Builder =
    addWebhook(Webhook.Type.EVENT, webhookBuilder(properties))

/**
 * Adds an `event_url` webhook to the [Rtc] capability.
 *
 * @param properties A lambda function for setting additional properties on the webhook.
 *
 * @return The RTC capability builder.
 */
fun Rtc.Builder.event(properties: Webhook.Builder.() -> Unit): Rtc.Builder =
    addWebhook(Webhook.Type.EVENT, webhookBuilder(properties))

/**
 * Adds a `status_url` webhook to the [Verify] capability.
 *
 * @param properties A lambda function for setting additional properties on the webhook.
 *
 * @return The Verify capability builder.
 */
fun Verify.Builder.status(properties: Webhook.Builder.() -> Unit): Verify.Builder =
    addWebhook(Webhook.Type.STATUS, webhookBuilder(properties))

/**
 * Adds an `inbound_url` webhook to the [Messages] capability.
 *
 * @param properties A lambda function for setting additional properties on the webhook.
 *
 * @return The Messages capability builder.
 */
fun Messages.Builder.inbound(properties: Webhook.Builder.() -> Unit): Messages.Builder =
    addWebhook(Webhook.Type.INBOUND, webhookBuilder(properties))

/**
 * Adds an `status_url` webhook to the [Messages] capability.
 *
 * @param properties A lambda function for setting additional properties on the webhook.
 *
 * @return The Messages capability builder.
 */
fun Messages.Builder.status(properties: Webhook.Builder.() -> Unit): Messages.Builder =
    addWebhook(Webhook.Type.STATUS, webhookBuilder(properties))

/**
 * Removes one or more capabilities from the application, used in conjunction with
 * [com.vonage.client.kt.Application.ExistingApplication#update].
 *
 * @param capabilities The capability types to remove.
 *
 * @return The application builder.
 */
fun Application.Builder.removeCapabilities(vararg capabilities: Capability.Type): Application.Builder {
    for (capability in capabilities) {
        removeCapability(capability)
    }
    return this
}

/**
 * Adds a [Voice] capability to the application.
 *
 * @param capability A lambda function for setting additional properties on the capability.
 *
 * @return The application builder.
 */
fun Application.Builder.voice(capability: Voice.Builder.() -> Unit): Application.Builder =
    addCapability(Voice.builder().apply(capability).build())

/**
 * Adds a [Messages] capability to the application.
 *
 * @param capability A lambda function for setting additional properties on the capability.
 *
 * @return The application builder.
 */
fun Application.Builder.messages(capability: Messages.Builder.() -> Unit): Application.Builder =
    addCapability(Messages.builder().apply(capability).build())

/**
 * Adds a [Verify] capability to the application.
 *
 * @param capability A lambda function for setting additional properties on the capability.
 *
 * @return The application builder.
 */
fun Application.Builder.verify(capability: Verify.Builder.() -> Unit): Application.Builder =
    addCapability(Verify.builder().apply(capability).build())

/**
 * Adds an [Rtc] capability to the application.
 *
 * @param capability A lambda function for setting additional properties on the capability.
 *
 * @return The application builder.
 */
fun Application.Builder.rtc(capability: Rtc.Builder.() -> Unit): Application.Builder =
    addCapability(Rtc.builder().apply(capability).build())

/**
 * Adds a [Vbc] capability to the application.
 *
 * @return The application builder.
 */
fun Application.Builder.vbc(): Application.Builder = addCapability(Vbc.builder().build())
