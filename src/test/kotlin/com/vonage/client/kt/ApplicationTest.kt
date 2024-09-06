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

import com.vonage.client.application.Application
import com.vonage.client.application.ApplicationResponseException
import com.vonage.client.application.capabilities.Capability
import com.vonage.client.application.capabilities.Region
import com.vonage.client.common.HttpMethod
import com.vonage.client.common.Webhook
import kotlin.test.*

class ApplicationTest : AbstractTest() {
    private val client = vonage.application
    private val authType = AuthType.API_KEY_SECRET_HEADER
    private val existingApplication = client.application(testUuid)
    private val baseUrl = "/v2/applications"
    private val appUrl = "$baseUrl/$testUuid"
    private val answerUrl = "$exampleUrlBase/answer"
    private val page = 3
    private val pageSize = 25
    private val connectionTimeout = 500
    private val socketTimeout = 3000
    private val region = Region.EU_WEST
    private val signedCallbacks = true
    private val answerMethod = HttpMethod.GET
    private val eventMethod = HttpMethod.POST
    private val statusMethod = HttpMethod.POST
    private val inboundMethod = HttpMethod.POST
    private val fallbackAnswerUrl = "$answerUrl-fallback"
    private val conversationsTtl = 12
    private val name = "My Application"
    private val publicKey = "-----BEGIN PUBLIC KEY-----\npublic key\n-----END PUBLIC KEY-----"
    private val improveAi = false
    private val basicApplicationRequest = mapOf(
        "name" to name,
        "keys" to mapOf(
            "public_key" to publicKey
        )
    )
    private val advancedApplicationProperties = mapOf(
        "capabilities" to mapOf(
            "voice" to mapOf(
                "webhooks" to mapOf(
                    "answer_url" to mapOf(
                        "address" to answerUrl,
                        "http_method" to answerMethod,
                        "connection_timeout" to connectionTimeout,
                        "socket_timeout" to socketTimeout
                    ),
                    "fallback_answer_url" to mapOf(
                        "address" to fallbackAnswerUrl,
                        "http_method" to answerMethod,
                        "connection_timeout" to connectionTimeout,
                        "socket_timeout" to socketTimeout
                    ),
                    "event_url" to mapOf(
                        "address" to eventUrl,
                        "http_method" to eventMethod,
                        "connection_timeout" to connectionTimeout,
                        "socket_timeout" to socketTimeout
                    )
                ),
                "signed_callbacks" to signedCallbacks,
                "conversations_ttl" to conversationsTtl,
                "region" to region
            ),
            "rtc" to mapOf(
                "webhooks" to mapOf(
                    "event_url" to mapOf(
                        "address" to eventUrl,
                        "http_method" to eventMethod
                    )
                )
            ),
            "messages" to mapOf(
                "webhooks" to mapOf(
                    "inbound_url" to mapOf(
                        "address" to moCallbackUrl,
                        "http_method" to inboundMethod
                    ),
                    "status_url" to mapOf(
                        "address" to statusCallbackUrl,
                        "http_method" to statusMethod
                    )
                )
            ),
            "vbc" to emptyMap(),
            "verify" to mapOf(
                "webhooks" to mapOf(
                    "status_url" to mapOf(
                        "address" to statusCallbackUrl,
                        "http_method" to statusMethod
                    )
                )
            )
        ),
        "privacy" to mapOf(
            "improve_ai" to improveAi
        )
    )
    private val advancedApplicationRequest = basicApplicationRequest + advancedApplicationProperties
    private val applicationResponseIdOnly = mapOf("id" to testUuid)
    private val basicApplicationResponse = applicationResponseIdOnly + basicApplicationRequest
    private val fullApplicationResponse = applicationResponseIdOnly + advancedApplicationRequest


    private fun assertEqualsIdOnlyApplication(parsed: Application) {
        assertNotNull(parsed)
        assertEquals(testUuidStr, parsed.id)
    }

    private fun assertEqualsBasicApplication(parsed: Application, name: String = this.name) {
        assertEqualsIdOnlyApplication(parsed)
        assertEquals(name, parsed.name)
        assertNotNull(parsed.keys)
        assertEquals(publicKey, parsed.keys.publicKey)
    }

    private fun assertEqualsFullApplication(parsed: Application) {
        assertEqualsBasicApplication(parsed)
        assertNotNull(parsed.privacy)
        assertEquals(improveAi, parsed.privacy.improveAi)
        val capabilities = parsed.capabilities
        assertNotNull(capabilities)
        
        val voice = capabilities.voice
        assertNotNull(voice)
        val voiceWebhooks = voice.webhooks
        
        assertNotNull(voiceWebhooks)
        val voiceAnswer = voiceWebhooks[Webhook.Type.ANSWER]
        assertNotNull(voiceAnswer)
        assertEquals(answerUrl, voiceAnswer.address)
        assertEquals(answerMethod, voiceAnswer.method)
        assertEquals(connectionTimeout, voiceAnswer.connectionTimeout)
        assertEquals(socketTimeout, voiceAnswer.socketTimeout)
        
        val fallbackAnswer = voiceWebhooks[Webhook.Type.FALLBACK_ANSWER]
        assertNotNull(fallbackAnswer)
        assertEquals(fallbackAnswerUrl, fallbackAnswer.address)
        assertEquals(answerMethod, fallbackAnswer.method)
        assertEquals(connectionTimeout, fallbackAnswer.connectionTimeout)
        assertEquals(socketTimeout, fallbackAnswer.socketTimeout)
        
        val voiceEvent = voiceWebhooks[Webhook.Type.EVENT]
        assertNotNull(voiceEvent)
        assertEquals(eventUrl, voiceEvent.address)
        assertEquals(eventMethod, voiceEvent.method)
        assertEquals(connectionTimeout, voiceEvent.connectionTimeout)
        assertEquals(socketTimeout, voiceEvent.socketTimeout)
        
        assertEquals(signedCallbacks, voice.signedCallbacks)
        assertEquals(conversationsTtl, voice.conversationsTtl)
        assertEquals(region, voice.region)
        
        val rtc = capabilities.rtc
        assertNotNull(rtc)
        val rtcEvent = rtc.webhooks[Webhook.Type.EVENT]
        assertNotNull(rtcEvent)
        assertEquals(eventUrl, rtcEvent.address)
        assertEquals(eventMethod, rtcEvent.method)
        
        val messages = capabilities.messages
        assertNotNull(messages)
        val inbound = messages.webhooks[Webhook.Type.INBOUND]
        assertNotNull(inbound)
        assertEquals(moCallbackUrl, inbound.address)
        assertEquals(inboundMethod, inbound.method)
        
        val status = messages.webhooks[Webhook.Type.STATUS]
        assertNotNull(status)
        assertEquals(statusCallbackUrl, status.address)
        assertEquals(statusMethod, status.method)
        
        val verify = capabilities.verify
        assertNotNull(verify)
        val verifyStatus = verify.webhooks[Webhook.Type.STATUS]
        assertNotNull(verifyStatus)
        assertEquals(statusCallbackUrl, verifyStatus.address)
        assertEquals(statusMethod, verifyStatus.method)
        
        assertNotNull(capabilities.vbc)
        assertEquals(improveAi, parsed.privacy.improveAi)
    }

    private fun assertEqualsBlankApplication(parsed: Application) {
        assertNotNull(parsed)
        assertNull(parsed.id)
        assertNull(parsed.name)
        assertNull(parsed.capabilities)
        assertNull(parsed.keys)
        assertNull(parsed.privacy)
    }

    private fun assertListApplications(filter: Map<String, Any> = mapOf(), invocation: () -> List<Application>) {
        val totalItems = 1337
        val totalPages = 54
        mockGet(
            expectedUrl = baseUrl, authType = authType, expectedQueryParams = filter,
            expectedResponseParams = mapOf(
                "page_size" to pageSize,
                "page" to page,
                "total_items" to totalItems,
                "total_pages" to totalPages,
                "_embedded" to mapOf(
                    "applications" to listOf(
                        applicationResponseIdOnly,
                        emptyMap(),
                        fullApplicationResponse,
                        basicApplicationResponse
                    )
                )
            )
        )
        val response = invocation()
        assertNotNull(response)
        assertEquals(4, response.size)
        assertEqualsIdOnlyApplication(response[0])
        assertEqualsBlankApplication(response[1])
        assertEqualsFullApplication(response[2])
        assertEqualsBasicApplication(response[3])

        assert401ApiResponseException<ApplicationResponseException>(baseUrl, HttpMethod.GET, invocation)
    }

    @BeforeTest
    fun init() {
        mockGet(
            expectedUrl = appUrl, authType = authType,
            expectedResponseParams = fullApplicationResponse
        )
    }

    @Test
    fun `get application all parameters`() {
        assertEqualsFullApplication(existingApplication.get())

        assert401ApiResponseException<ApplicationResponseException>(appUrl, HttpMethod.GET) {
            existingApplication.get()
        }
    }

    @Test
    fun `delete application`() {
        mockDelete(expectedUrl = appUrl, authType = authType)
        existingApplication.delete()

        assert401ApiResponseException<ApplicationResponseException>(appUrl, HttpMethod.DELETE) {
            existingApplication.delete()
        }
    }

    @Test
    fun `list all applications`() {
        assertListApplications { client.listAll() }
    }

    @Test
    fun `list applications no filter`() {
        assertListApplications { client.list() }
    }

    @Test
    fun `list applications all filters`() {
        assertListApplications(mapOf("page" to page, "page_size" to pageSize)) {
            client.list(page, pageSize)
        }
    }

    @Test
    fun `update application`() {
        val newName = "New Name"
        val plainApp = basicApplicationResponse.toMutableMap()
        plainApp["name"] = newName
        plainApp["capabilities"] = mapOf("vbc" to emptyMap<String, Any>())

        mockPut(
            expectedUrl = appUrl, authType = authType,
            expectedRequestParams = plainApp,
            expectedResponseParams = plainApp
        )
        val response = existingApplication.update {
            name(newName)
            removeCapabilities(
                Capability.Type.VOICE,
                Capability.Type.RTC,
                Capability.Type.MESSAGES,
                Capability.Type.VERIFY
            )
        }
        assertEqualsBasicApplication(response, newName)

        assert401ApiResponseException<ApplicationResponseException>(appUrl, HttpMethod.PUT) {
            existingApplication.update {}
        }
    }

    @Test
    fun `create application with all capabilities and webhooks`() {
        mockPost(
            expectedUrl = baseUrl, authType = authType,
            expectedRequestParams = advancedApplicationRequest,
            expectedResponseParams = fullApplicationResponse
        )
        assertEqualsFullApplication(client.create {
            name(name)
            publicKey(publicKey)
            voice {
                answer {
                    url(answerUrl)
                    method(answerMethod)
                    connectionTimeout(connectionTimeout)
                    socketTimeout(socketTimeout)
                }
                fallbackAnswer {
                    url(fallbackAnswerUrl)
                    method(answerMethod)
                    connectionTimeout(connectionTimeout)
                    socketTimeout(socketTimeout)
                }
                event {
                    url(eventUrl)
                    method(eventMethod)
                    connectionTimeout(connectionTimeout)
                    socketTimeout(socketTimeout)
                }
                signedCallbacks(signedCallbacks)
                conversationsTtl(conversationsTtl)
                region(region)
            }
            rtc {
                event {
                    url(eventUrl)
                    method(eventMethod)
                }
            }
            messages {
                inbound {
                    url(moCallbackUrl)
                    method(inboundMethod)
                }
                status {
                    url(statusCallbackUrl)
                    method(statusMethod)
                }
            }
            vbc()
            verify {
                status {
                    url(statusCallbackUrl)
                    method(statusMethod)
                }
            }
            improveAi(improveAi)
        })

        assert401ApiResponseException<ApplicationResponseException>(baseUrl, HttpMethod.POST) {
            client.create { name(name) }
        }
    }
}