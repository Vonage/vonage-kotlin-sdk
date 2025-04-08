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

import com.vonage.client.common.HttpMethod
import com.vonage.client.verify2.Channel
import com.vonage.client.verify2.FragmentChannel
import com.vonage.client.verify2.Template
import com.vonage.client.verify2.TemplateFragment
import com.vonage.client.verify2.VerifyResponseException
import java.net.URI
import java.util.Locale
import java.util.UUID
import kotlin.test.*

class VerifyTest : AbstractTest() {
    private val client = vonage.verify
    private val baseUrl = "/v2/verify"
    private val requestIdStr = "c11236f4-00bf-4b89-84ba-88b25df97315"
    private val requestId = UUID.fromString(requestIdStr)
    private val requestIdUrl = "$baseUrl/$requestIdStr"
    private val existingRequest = client.request(requestIdStr)
    private val timeout = 60
    private val fraudCheck = false
    private val sandbox = true
    private val codeLength = 5
    private val code = "1228864"
    private val locale = "ja-jp"
    private val whatsappNumber = "447700400080"
    private val appHash = "ABC123def45"
    private val toEmail = "alice@example.com"
    private val fromEmail = "bob@example.org"
    private val checkUrl = "$apiBaseUrl$baseUrl/$requestIdStr/silent-auth/redirect"
    private val redirectUrl = "https://acme-app.com/sa/redirect"
    private val checkCodeRequestParams = mapOf("code" to code)
    private val templatesBaseUrl = "$baseUrl/templates"
    private val templateName = "my-template"
    private val templateId = "8f35a1a7-eb2f-4552-8fdf-fffdaee41bc9"
    private val templateUrl = "$templatesBaseUrl/$templateId"
    private val templateFragmentsBaseUrl = "$templateUrl/template_fragments"
    private val existingTemplate = client.template(templateId)
    private val template = mapOf(
        "template_id" to templateId,
        "name" to templateName,
        "is_default" to true,
        "_links" to mapOf(
            "self" to mapOf(
                "href" to "$apiBaseUrl$templateUrl"
            ),
            "fragments" to mapOf(
                "href" to "$apiBaseUrl$templateFragmentsBaseUrl"
            )
        )
    )
    private val fragmentId = "c70f446e-997a-4313-a081-60a02a31dc19"
    private val fragmentUrl = "$templateFragmentsBaseUrl/$fragmentId"
    private val existingTemplateFragment = existingTemplate.fragment(fragmentId)
    private val fragmentText = "Text content of the template. May contain 4 reserved variables:" +
            "`\${code}`, `\${brand}`, `\${time-limit}` and `\${time-limit-unit}`"
    private val fragmentChannel = FragmentChannel.SMS
    private val fragmentResponse = mapOf(
        "template_fragment_id" to fragmentId,
        "channel" to fragmentChannel,
        "locale" to locale,
        "text" to fragmentText,
        "date_updated" to timestampStr,
        "date_created" to timestamp2Str,
        "_links" to mapOf(
            "self" to mapOf(
                "href" to "$apiBaseUrl$fragmentUrl"
            ),
            "template" to mapOf(
                "href" to "$apiBaseUrl$templatesBaseUrl"
            )
        )
    )


    private fun assertVerifyResponseException(url: String, requestMethod: HttpMethod, actualCall: () -> Any) {
        assertApiResponseException<VerifyResponseException>(url, requestMethod, actualCall)
        if (url.contains(requestIdStr)) {
            assertApiResponseException<VerifyResponseException>(url, requestMethod, actualCall,
                404, errorType = "https://developer.vonage.com/api-errors#not-found",
                title = "Not Found", instance = "bf0ca0bf927b3b52e3cb03217e1a1ddf",
                detail = "Request $requestIdStr was not found or it has been verified already."
            )
            if (requestMethod != HttpMethod.DELETE) {
                assertApiResponseException<VerifyResponseException>(url, requestMethod, actualCall,
                    409, errorType = "https://www.developer.vonage.com/api-errors/verify#conflict",
                    title = "Conflict", instance = "738f9313-418a-4259-9b0d-6670f06fa82d",
                    detail = "Concurrent verifications to the same number are not allowed."
                )
            }
        }
    }

    private fun assertEqualsSampleTemplate(parsed: Template) {
        assertNotNull(parsed)
        assertEquals(UUID.fromString(templateId), parsed.id)
        assertEquals(templateName, parsed.name)
        assertTrue(parsed.isDefault)
    }

    private fun assertEqualsSampleTemplateFragment(parsed: TemplateFragment) {
        assertNotNull(parsed)
        assertEquals(UUID.fromString(fragmentId), parsed.fragmentId)
        assertEquals(fragmentChannel, parsed.channel)
        assertEquals(Locale.forLanguageTag(locale), parsed.locale)
        assertEquals(fragmentText, parsed.text)
        assertEquals(timestamp, parsed.dateUpdated)
        assertEquals(timestamp2, parsed.dateCreated)
    }

    @Test
    fun `send verification single workflow required parameters`() {
        for (channel in Channel.entries) {
            mockPost(
                baseUrl, status = 202, expectedRequestParams = mapOf(
                    "brand" to brand, "workflow" to listOf(
                        mapOf(
                            "channel" to channel.toString(),
                            "to" to if (channel == Channel.EMAIL) toEmail else toNumber
                        ) + when (channel) {
                            Channel.WHATSAPP -> mapOf("from" to whatsappNumber)
                            else -> mapOf()
                        }
                    )
                ),
                expectedResponseParams = mapOf("request_id" to requestIdStr) +
                        if (channel == Channel.SILENT_AUTH) mapOf("check_url" to checkUrl) else mapOf()
            )

            val response = client.sendVerification(brand) {
                when (channel) {
                    Channel.VOICE -> voice(toNumber)
                    Channel.SMS -> sms(toNumber)
                    Channel.SILENT_AUTH -> silentAuth(toNumber)
                    Channel.EMAIL -> email(toEmail)
                    Channel.WHATSAPP -> whatsapp(toNumber, whatsappNumber)
                }
            }
            assertNotNull(response)
            assertEquals(requestId, response.requestId)
            if (channel == Channel.SILENT_AUTH) {
                assertEquals(URI.create(checkUrl), response.checkUrl)
            }
            else {
                assertNull(response.checkUrl)
            }
        }
    }

    @Test
    fun `send verification all workflows and parameters`() {
        mockPost(baseUrl,
            expectedRequestParams = mapOf(
                "brand" to brand,
                "client_ref" to clientRef,
                "channel_timeout" to timeout,
                "code_length" to codeLength,
                "locale" to "ja-jp",
                "fraud_check" to fraudCheck,
                "workflow" to listOf(
                    mapOf(
                        "channel" to "silent_auth",
                        "to" to toNumber,
                        "sandbox" to sandbox,
                        "redirect_url" to redirectUrl
                    ),
                    mapOf(
                        "channel" to "voice",
                        "to" to altNumber
                    ),
                    mapOf(
                        "channel" to "sms",
                        "to" to toNumber,
                        "from" to altNumber,
                        "content_id" to contentId,
                        "entity_id" to entityId,
                        "app_hash" to appHash
                    ),
                    mapOf(
                        "channel" to "email",
                        "to" to toEmail,
                        "from" to fromEmail
                    ),
                    mapOf(
                        "channel" to "whatsapp",
                        "to" to altNumber,
                        "from" to whatsappNumber
                    )
                )
            ),
            expectedResponseParams = mapOf(
                "request_id" to requestIdStr,
                "check_url" to checkUrl
            ),
            status = 202
        )

        val response = client.sendVerification {
            brand(brand); clientRef(clientRef); channelTimeout(timeout)
            fraudCheck(fraudCheck); codeLength(codeLength); locale(locale)
            silentAuth(toNumber, sandbox, redirectUrl); voice(altNumber)
            sms(toNumber) {
                entityId(entityId); contentId(contentId); appHash(appHash); from(altNumber)
            }
            email(toEmail, fromEmail)
            whatsapp(altNumber, whatsappNumber)
        }

        assertNotNull(response)
        assertEquals(requestId, response.requestId)
        assertEquals(URI.create(checkUrl), response.checkUrl)
    }

    @Test
    fun `cancel verification`() {
        mockDelete(requestIdUrl)
        existingRequest.cancel()
        assertVerifyResponseException(requestIdUrl, HttpMethod.DELETE) {
            existingRequest.cancel()
        }
    }

    @Test
    fun `next workflow`() {
        val expectedUrl = "$requestIdUrl/next-workflow"
        mockPost(expectedUrl)
        existingRequest.nextWorkflow()
        assertVerifyResponseException(expectedUrl, HttpMethod.POST) {
            existingRequest.nextWorkflow()
        }
        assertVerifyResponseException(expectedUrl, HttpMethod.POST) {
            existingRequest.nextWorkflow()
        }
    }

    @Test
    fun `check valid verification code`() {
        val call: () -> Boolean = {
            existingRequest.isValidVerificationCode(code)
        }

        mockPost(requestIdUrl, checkCodeRequestParams, 200)
        assertTrue(call.invoke())
        existingRequest.checkVerificationCode(code)

        val title = "Invalid Code"

        mockPost(requestIdUrl, checkCodeRequestParams, 400, expectedResponseParams = mapOf(
            "title" to title,
            "type" to "https://www.developer.vonage.com/api-errors/verify#invalid-code",
            "detail" to "The code you provided does not match the expected value."
        ))
        assertFalse(call.invoke())

        mockPost(requestIdUrl, checkCodeRequestParams, 410, expectedResponseParams = mapOf(
            "title" to title,
            "type" to "https://www.developer.vonage.com/api-errors/verify#expired",
            "detail" to "An incorrect code has been provided too many times. Workflow terminated."
        ))
        assertFalse(call.invoke())

        assertVerifyResponseException(requestIdUrl, HttpMethod.POST, call)
        assertVerifyResponseException(requestIdUrl, HttpMethod.POST) {
            existingRequest.checkVerificationCode(code)
        }
    }

    @Test
    fun `list templates`() {
        val templateId2 = "0ac50843-b549-4a89-916e-848749f20040"
        val templateName2 = "my-template-2"
        mockGet(templatesBaseUrl,
            expectedQueryParams = mapOf("page" to 1, "page_size" to 100),
            expectedResponseParams = mapOf(
                "page_size" to 1,
                "page" to 2,
                "total_pages" to 10,
                "total_items" to 25,
                "_embedded" to mapOf(
                    "templates" to listOf(
                        template,
                        emptyMap(),
                        mapOf(
                            "template_id" to templateId2,
                            "name" to templateName2,
                            "is_default" to false,
                            "_links" to mapOf(
                                "self" to mapOf(
                                    "href" to "$apiBaseUrl$templatesBaseUrl/$templateId2"
                                ),
                                "fragments" to mapOf(
                                    "href" to "$apiBaseUrl$templatesBaseUrl/$templateId2/template_fragments"
                                )
                            )
                        )
                    )
                ),
                "_links" to mapOf(
                    "self" to mapOf(
                        "href" to "$templatesBaseUrl?page=2"
                    ),
                    "next" to mapOf(
                        "href" to "$templatesBaseUrl?page=3"
                    ),
                    "prev" to mapOf(
                        "href" to "$templatesBaseUrl?page=1"
                    ),
                    "last" to mapOf(
                        "href" to "$templatesBaseUrl?page=5"
                    )
                )
            )
        )
        val templates = client.listTemplates()
        assertNotNull(templates)
        assertEquals(3, templates.size)
        assertEqualsSampleTemplate(templates[0])

        val emptyTemplate = templates[1]
        assertNotNull(emptyTemplate)
        assertNull(emptyTemplate.id)
        assertNull(emptyTemplate.name)
        assertNull(emptyTemplate.isDefault)

        val template2 = templates[2]
        assertNotNull(template2)
        assertEquals(UUID.fromString(templateId2), template2.id)
        assertEquals(templateName2, template2.name)
        assertFalse(template2.isDefault)
    }

    @Test
    fun `create template`() {
        val name = "My_custom_template-testRequestKt"
        mockPost(templatesBaseUrl, status = 201,
            expectedRequestParams = mapOf("name" to name),
            expectedResponseParams = template
        )
        val response = client.createTemplate(name)
        assertEqualsSampleTemplate(response)
    }

    @Test
    fun `update template`() {
        val name = "updated-legacy_template"
        val nameMap = mapOf("name" to name)
        val isDefaultMap = mapOf("is_default" to false)

        mockPatch(templateUrl,
            expectedRequestParams = nameMap + isDefaultMap,
            expectedResponseParams = template
        )
        assertEqualsSampleTemplate(existingTemplate.update(name, false))

        mockPatch(templateUrl,
            expectedRequestParams = nameMap,
            expectedResponseParams = template
        )
        assertEqualsSampleTemplate(existingTemplate.update(name = name))

        mockPatch(templateUrl,
            expectedRequestParams = isDefaultMap,
            expectedResponseParams = template
        )
        assertEqualsSampleTemplate(existingTemplate.update(isDefault = false))
    }

    @Test
    fun `get template`() {
        mockGet(templateUrl, expectedResponseParams = template)
        assertEqualsSampleTemplate(existingTemplate.get())
    }

    @Test
    fun `delete template`() {
        mockDelete(templateUrl)
        existingTemplate.delete()
        assertVerifyResponseException(templateUrl, HttpMethod.DELETE) {
            existingTemplate.delete()
        }
    }

    @Test
    fun `list template fragments`() {
        mockGet(templateFragmentsBaseUrl,
            expectedQueryParams = mapOf("page" to 1, "page_size" to 1000),
            expectedResponseParams = mapOf(
                "page_size" to 1,
                "page" to 2,
                "total_pages" to 10,
                "total_items" to 25,
                "_embedded" to mapOf(
                    "template_fragments" to listOf(
                        fragmentResponse,
                        emptyMap()
                    )
                ),
                "_links" to mapOf(
                    "self" to mapOf(
                        "href" to "$templateFragmentsBaseUrl?page=2"
                    ),
                    "next" to mapOf(
                        "href" to "$templateFragmentsBaseUrl?page=3"
                    ),
                    "prev" to mapOf(
                        "href" to "$templateFragmentsBaseUrl?page=1"
                    ),
                    "last" to mapOf(
                        "href" to "$templateFragmentsBaseUrl?page=5"
                    )
                )
            )
        )
        val fragments = existingTemplate.listFragments()
        assertNotNull(fragments)
        assertEquals(2, fragments.size)
        assertEqualsSampleTemplateFragment(fragments[0])

        val emptyFragment = fragments[1]
        assertNotNull(emptyFragment)
        assertNull(emptyFragment.fragmentId)
        assertNull(emptyFragment.templateId)
        assertNull(emptyFragment.channel)
        assertNull(emptyFragment.locale)
        assertNull(emptyFragment.text)
        assertNull(emptyFragment.dateUpdated)
        assertNull(emptyFragment.dateCreated)
    }

    @Test
    fun `create template fragment`() {
        mockPost(templateFragmentsBaseUrl, status = 201,
            expectedRequestParams = mapOf(
                "channel" to fragmentChannel.name.lowercase(),
                "locale" to locale,
                "text" to fragmentText
            ),
            expectedResponseParams = fragmentResponse
        )
        assertEqualsSampleTemplateFragment(
            existingTemplate.createFragment(
                text = fragmentText,
                channel = fragmentChannel,
                locale = locale
            )
        )
    }

    @Test
    fun `update template fragment`() {
        val newText = "The authentication code for your \${brand} is: \${code}"
        mockPatch(fragmentUrl,
            expectedRequestParams = mapOf("text" to newText),
            expectedResponseParams = fragmentResponse
        )
        assertEqualsSampleTemplateFragment(existingTemplateFragment.update(newText))
    }

    @Test
    fun `get template fragment`() {
        mockGet(fragmentUrl, expectedResponseParams = fragmentResponse)
        assertEqualsSampleTemplateFragment(existingTemplateFragment.get())
    }

    @Test
    fun `delete template fragment`() {
        mockDelete(fragmentUrl)
        existingTemplateFragment.delete()
    }
}