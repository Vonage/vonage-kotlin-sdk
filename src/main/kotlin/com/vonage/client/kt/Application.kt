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

class Application internal constructor(private val client: ApplicationClient) {

    fun application(applicationId: String): ExistingApplication = ExistingApplication(applicationId)

    fun application(applicationId: UUID) = application(applicationId.toString())

    inner class ExistingApplication internal constructor(id: String): ExistingResource(id) {
        fun get(): Application = client.getApplication(id)

        fun update(properties: Application.Builder.() -> Unit): Application =
            client.updateApplication(Application.builder(get()).apply(properties).build())

        fun delete(): Unit = client.deleteApplication(id)
    }

    fun listAll(): List<Application> = client.listAllApplications()

    fun list(page: Int? = null, pageSize: Int? = null): List<Application> {
        val filter = ListApplicationRequest.builder()
        if (page != null) filter.page(page.toLong())
        if (pageSize != null) filter.pageSize(pageSize.toLong())
        return client.listApplications(filter.build()).applications
    }

    fun create(properties: Application.Builder.() -> Unit): Application =
        client.createApplication(Application.builder().apply(properties).build())
}

private fun webhookBuilder(properties: Webhook.Builder.() -> Unit): Webhook =
    Webhook.builder().apply(properties).build()

fun Webhook.Builder.url(url: String): Webhook.Builder = address(url)

fun Voice.Builder.answer(properties: Webhook.Builder.() -> Unit): Voice.Builder =
    addWebhook(Webhook.Type.ANSWER, webhookBuilder(properties))

fun Voice.Builder.fallbackAnswer(properties: Webhook.Builder.() -> Unit): Voice.Builder =
    addWebhook(Webhook.Type.FALLBACK_ANSWER, webhookBuilder(properties))

fun Voice.Builder.event(properties: Webhook.Builder.() -> Unit): Voice.Builder =
    addWebhook(Webhook.Type.EVENT, webhookBuilder(properties))

fun Rtc.Builder.event(properties: Webhook.Builder.() -> Unit): Rtc.Builder =
    addWebhook(Webhook.Type.EVENT, webhookBuilder(properties))

fun Verify.Builder.status(properties: Webhook.Builder.() -> Unit): Verify.Builder =
    addWebhook(Webhook.Type.STATUS, webhookBuilder(properties))

fun Messages.Builder.inbound(properties: Webhook.Builder.() -> Unit): Messages.Builder =
    addWebhook(Webhook.Type.INBOUND, webhookBuilder(properties))

fun Messages.Builder.status(properties: Webhook.Builder.() -> Unit): Messages.Builder =
    addWebhook(Webhook.Type.STATUS, webhookBuilder(properties))

fun Application.Builder.removeCapabilities(vararg capabilities: Capability.Type): Application.Builder {
    for (capability in capabilities) {
        removeCapability(capability)
    }
    return this
}

fun Application.Builder.voice(capability: Voice.Builder.() -> Unit): Application.Builder =
    addCapability(Voice.builder().apply(capability).build())

fun Application.Builder.messages(capability: Messages.Builder.() -> Unit): Application.Builder =
    addCapability(Messages.builder().apply(capability).build())

fun Application.Builder.verify(capability: Verify.Builder.() -> Unit): Application.Builder =
    addCapability(Verify.builder().apply(capability).build())

fun Application.Builder.rtc(capability: Rtc.Builder.() -> Unit): Application.Builder =
    addCapability(Rtc.builder().apply(capability).build())

fun Application.Builder.vbc(): Application.Builder = addCapability(Vbc.builder().build())
