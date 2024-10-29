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

import com.vonage.client.verify2.*
import java.util.*

/**
 * Implementation of the [Verify v2 API](https://developer.vonage.com/en/api/verify.v2).
 *
 * *Authentication method:* JWT (recommended) and API key & secret (limited functionality).
 */
class Verify(private val client: Verify2Client) {

    /**
     * Initiate a verification request.
     *
     * @param brand The name of the company or app sending the verification request.
     * This is what the user will see the sender as on their device.
     *
     * @param properties A lambda function for specifying the verification request properties.
     * You must set at least one workflow and a recipient. See [VerificationRequest.Builder] methods for details.
     *
     * @return A [VerificationResponse] object containing the request ID.
     *
     * @throws [VerifyResponseException] If the request could not be sent. This may be for the following reasons:
     * - **401**: Invalid credentials.
     * - **402**: Account balance too low.
     * - **409**: Verification already in progress. You must wait for it to be completed or aborted.
     * - **422**: Invalid parameters. Check the error message for details.
     * - **429**: Rate limit exceeded.
     * - **500**: Internal server error.
     */
    fun sendVerification(brand: String = "Vonage",
                         properties: VerificationRequest.Builder.() -> Unit): VerificationResponse =
        client.sendVerification(VerificationRequest.builder().brand(brand).apply(properties).build())

    /**
     * Call this method to work with an existing verification request.
     *
     * @param requestId UUID of the verification request to work with,
     * as obtained from [VerificationResponse.getRequestId].
     *
     * @return An [ExistingRequest] object with methods to interact with the request.
     */
    fun request(requestId: UUID): ExistingRequest = ExistingRequest(requestId)

    /**
     * Call this method to work with an existing verification request.
     *
     * @param requestId UUID of the verification request to work with.
     */
    fun request(requestId: String): ExistingRequest = request(UUID.fromString(requestId))

    /**
     * Class for working with an existing verification request.
     *
     * @param uuid UUID of the verification request to work with, as obtained from [VerificationResponse.getRequestId].
     */
    inner class ExistingRequest internal constructor(private val uuid: UUID): ExistingResource(uuid.toString()) {

        /**
         * Cancels the verification request.
         *
         * @throws [VerifyResponseException] If the request could not be cancelled.
         * This may be for the following reasons:
         * - **401**: Invalid credentials.
         * - **402**: Account balance too low.
         * - **404**: Verification request not found.
         * - **500**: Internal server error.
         */
        fun cancel(): Unit = client.cancelVerification(uuid)

        /**
         * Advanced the verification request to the next workflow.
         *
         * @throws [VerifyResponseException] If the request could not be moved to the next worklow.
         * This could be for the following reasons:
         * - **401**: Invalid credentials.
         * - **402**: Account balance too low.
         * - **404**: Verification request not found.
         * - **409**: Verification already completed or cancelled.
         * - **500**: Internal server error.
         */
        fun nextWorkflow(): Unit = client.nextWorkflow(uuid)

        /**
         * Checks the verification code. If successful (the code matches), this method will return normally.
         * If the code does not match, an exception will be thrown.
         *
         * @param code The verification code to check, as entered by the user.
         *
         * @return A [VerifyCodeResponse] object containing the verification status.
         *
         * @throws [VerifyResponseException] If the check fails. This could be for the following reasons:
         * - **400**: Invalid code.
         * - **401**: Invalid credentials.
         * - **402**: Account balance too low.
         * - **404**: Request was not found, or it has been verified already.
         * - **409**: The current workflow does not support a code.
         * - **410**: An incorrect code has been provided too many times. Workflow terminated.
         * - **429**: Rate limit exceeded.
         * - **500**: Internal server error.
         *
         * @see isValidVerificationCode For a Boolean-returning version of this method.
         */
        fun checkVerificationCode(code: String): VerifyCodeResponse =
            client.checkVerificationCode(uuid, code)

        /**
         * Checks the verification code. If successful (the code matches), this method will return `true`.
         * If the code does not match, the method will return `false. For any other case, an exception will be thrown.
         *
         * @param code The verification code to check, as entered by the user.
         *
         * @return `true` if the code is valid, `false` if it is not.
         *
         * @throws [VerifyResponseException] If the code could not be checked. This could be for the following reasons:
         * - **401**: Invalid credentials.
         * - **402**: Account balance too low.
         * - **404**: Request was not found, or it has been verified already.
         * - **409**: The current workflow does not support a code.
         * - **429**: Rate limit exceeded.
         * - **500**: Internal server error.
         *
         * @see checkVerificationCode For the original implementation which this method delegates to.
         */
        fun isValidVerificationCode(code: String): Boolean {
            try {
                checkVerificationCode(code)
                return true
            } catch (ex: VerifyResponseException) {
                if (ex.statusCode == 400 || ex.statusCode == 410) {
                    return false
                } else {
                    throw ex
                }
            }
        }
    }

    /**
     * Create a new custom template.
     *
     * @param name Reference name for the template. Must not contain spaces or special characters than `_` and `-`.
     *
     * @return Details of the created template.
     *
     * @throws [VerifyResponseException] If the template could not be created. This could be for the following reasons:
     * - **401**: Invalid credentials.
     * - **402**: Account balance too low.
     * - **403**: Template management is not enabled for your account.
     * - **409**: Template with the same name already exists, or you have 10 templates already.
     * - **429**: Rate limit exceeded.
     * - **500**: Internal server error.
     *
     * @since 1.1.0
     */
    fun createTemplate(name: String): Template = client.createTemplate(name)

    /**
     * List all custom templates for your application.
     *
     * @return A list of all custom templates.
     *
     * @throws [VerifyResponseException] If the templates could not be listed. This could be for the following reasons:
     * - **401**: Invalid credentials.
     * - **402**: Account balance too low.
     * - **429**: Rate limit exceeded.
     * - **500**: Internal server error.
     *
     * @since 1.1.0
     */
    fun listTemplates(): List<Template> = client.listTemplates()

    /**
     * Call this method to work with an existing custom template.
     *
     * @param templateId ID of the template to work with as a string.
     *
     * @return An [ExistingTemplate] object with methods to interact with the template.
     *
     * @since 1.1.0
     */
    fun template(templateId: String): ExistingTemplate = ExistingTemplate(templateId)

    /**
     * Class for working with an existing custom template.
     *
     * @param templateId UUID of the template to work with, as obtained from [Template.getId].
     *
     * @since 1.1.0
     */
    inner class ExistingTemplate internal constructor(id: String): ExistingResource(id) {
        private val templateId = UUID.fromString(id)

        /**
         * Get the custom template.
         *
         * @return Details of the custom template.
         *
         * @throws [VerifyResponseException] If the template could not be retrieved. This could be for the following reasons:
         * - **401**: Invalid credentials.
         * - **402**: Account balance too low.
         * - **404**: Template not found.
         * - **429**: Rate limit exceeded.
         * - **500**: Internal server error.
         *
         * @since 1.1.0
         */
        fun get(): Template = client.getTemplate(templateId)

        /**
         * Update the custom template. Note that you must specify at least one parameter to change.
         *
         * @param name (OPTIONAL) New reference name for the template.
         * Must not contain spaces or special characters besides `_` and `-`.
         *
         * @param isDefault (OPTIONAL) Whether to set this template as the default.
         *
         * @return Details of the updated template.
         *
         * @throws [VerifyResponseException] If the template could not be updated. This could be for the following reasons:
         * - **401**: Invalid credentials.
         * - **402**: Account balance too low.
         * - **403**: Template management is not enabled for your account.
         * - **404**: Template not found.
         * - **409**: Template with this name already exists.
         * - **429**: Rate limit exceeded.
         * - **500**: Internal server error.
         *
         * @since 1.1.0
         */
        fun update(name: String? = null, isDefault: Boolean? = null): Template =
            client.updateTemplate(templateId, name, isDefault)

        /**
         * Delete the custom template.
         *
         * @throws [VerifyResponseException] If the template could not be deleted. This could be for the following reasons:
         * - **401**: Invalid credentials.
         * - **402**: Account balance too low.
         * - **403**: Template management is not enabled for your account.
         * - **404**: Template not found.
         * - **409**: Template is the default or contains undeleted fragments.
         * - **429**: Rate limit exceeded.
         * - **500**: Internal server error.
         *
         * @since 1.1.0
         */
        fun delete(): Unit = client.deleteTemplate(templateId)

        /**
         * Create a new template fragment.
         *
         * @param text Text content of the template. There are 4 reserved variables available to use:
         * `${code}`, `${brand}`, `${time-limit}` and `${time-limit-unit}`. You must always use `${code}`.
         * @param locale BCP-47 locale of the fragment.
         * @param channel The channel for the fragment.
         *
         * @return Details of the created fragment.
         *
         * @throws [VerifyResponseException] If the fragment could not be created.
         * This could be for the following reasons:
         *
         * - **401**: Invalid credentials.
         * - **402**: Account balance too low.
         * - **403**: Template management is not enabled for your account.
         * - **404**: Template not found.
         * - **409**: Fragment with the same locale and channel already exists.
         * - **429**: Rate limit exceeded.
         * - **500**: Internal server error.
         *
         * @since 1.1.0
         */
        fun createFragment(text: String, locale: String, channel: FragmentChannel): TemplateFragment =
            client.createTemplateFragment(templateId, TemplateFragment(channel, locale, text))

        /**
         * List all template fragments for the template.
         *
         * @return A list of all template fragments.
         *
         * @throws [VerifyResponseException] If the fragments could not be listed.
         * This could be for the following reasons:
         * - **401**: Invalid credentials.
         * - **402**: Account balance too low.
         * - **404**: Template not found.
         * - **429**: Rate limit exceeded.
         * - **500**: Internal server error.
         *
         * @since 1.1.0
         */
        fun listFragments(): List<TemplateFragment> = client.listTemplateFragments(templateId)

        /**
         * Call this method to work with an existing template fragment.
         *
         * @param fragmentId ID of the fragment to work with as a string.
         *
         * @return An [ExistingTemplateFragment] object with methods to interact with the fragment.
         *
         * @since 1.1.0
         */
        fun fragment(fragmentId: String): ExistingTemplateFragment = ExistingTemplateFragment(fragmentId)

        /**
         * Class for working with an existing template fragment.
         *
         * @param fragmentId UUID of the fragment to work with, as obtained from [TemplateFragment.getId].
         *
         * @since 1.1.0
         */
        inner class ExistingTemplateFragment internal constructor(id: String): ExistingResource(id) {
            private val fragmentId = UUID.fromString(id)

            /**
             * Get the template fragment.
             *
             * @return Details of the template fragment.
             *
             * @throws [VerifyResponseException] If the fragment could not be retrieved.
             * This could be for the following reasons:
             * - **401**: Invalid credentials.
             * - **402**: Account balance too low.
             * - **404**: Fragment not found.
             * - **429**: Rate limit exceeded.
             * - **500**: Internal server error.
             *
             * @since 1.1.0
             */
            fun get(): TemplateFragment = client.getTemplateFragment(templateId, fragmentId)

            /**
             * Update the template fragment.
             *
             * @param text New text for the fragment. There are 4 reserved variables available to use:
             *`${code}`, `${brand}`, `${time-limit}` and `${time-limit-unit}`. You must always use `${code}`.
             *
             * @return Details of the updated fragment.
             *
             * @throws [VerifyResponseException] If the fragment could not be updated.
             * This could be for the following reasons:
             * - **401**: Invalid credentials.
             * - **402**: Account balance too low.
             * - **403**: Template management is not enabled for your account.
             * - **404**: Fragment not found.
             * - **429**: Rate limit exceeded.
             * - **500**: Internal server error.
             *
             * @since 1.1.0
             */
            fun update(text: String): TemplateFragment = client.updateTemplateFragment(templateId, fragmentId, text)

            /**
             * Delete the template fragment.
             *
             * @throws [VerifyResponseException] If the fragment could not be deleted.
             * This could be for the following reasons:
             * - **401**: Invalid credentials.
             * - **402**: Account balance too low.
             * - **403**: Template management is not enabled for your account.
             * - **404**: Fragment not found.
             * - **429**: Rate limit exceeded.
             * - **500**: Internal server error.
             *
             * @since 1.1.0
             */
            fun delete(): Unit = client.deleteTemplateFragment(templateId, fragmentId)
        }
    }
}

/**
 * Adds a Silent Authentication workflow to the verification request. Note that this must be the first workflow.
 *
 * @param number The recipient's phone number in E.164 format.
 *
 * @param sandbox (OPTIONAL) Whether to use the Vonage Sandbox (for testing purposes). Default is `false`.
 *
 * @param redirectUrl (OPTIONAL) Final redirect added at the end of the `check_url` request/response lifecycle.
 * Will contain the `request_id` and `code` as a URL fragment. This can be used to redirect the user back
 * to your application after following the verification link on their device.
 *
 * @return The verification request builder.
 */
fun VerificationRequest.Builder.silentAuth(
        number: String, sandbox: Boolean? = null, redirectUrl: String? = null): VerificationRequest.Builder {
    val builder = SilentAuthWorkflow.builder(number)
    if (sandbox != null) builder.sandbox(sandbox)
    if (redirectUrl != null) builder.redirectUrl(redirectUrl)
    return addWorkflow(builder.build())
}

/**
 * Adds an SMS workflow to the verification request.
 *
 * @param number The recipient's phone number in E.164 format.
 *
 * @param properties (OPTIONAL) A lambda function for specifying additional SMS workflow parameters.
 *
 * @return The verification request builder.
 */
fun VerificationRequest.Builder.sms(
        number: String, properties: SmsWorkflow.Builder.() -> Unit = {}): VerificationRequest.Builder =
    addWorkflow(SmsWorkflow.builder(number).apply(properties).build())

/**
 * Adds a TTS (Text-to-Speech) workflow to the verification request.
 *
 * @param number The recipient's phone number in E.164 format.
 *
 * @return The verification request builder.
 */
fun VerificationRequest.Builder.voice(number: String): VerificationRequest.Builder =
    addWorkflow(VoiceWorkflow(number))

/**
 * Adds an email workflow to the verification request.
 *
 * @param to The recipient's email address.
 *
 * @param from (OPTIONAL) The email address to send the request from. This is not available by default.
 */
fun VerificationRequest.Builder.email(to: String, from: String? = null): VerificationRequest.Builder =
    addWorkflow(EmailWorkflow(to, from))

/**
 * Adds a WhatsApp text workflow to the verification request.
 *
 * @param to The recipient's phone number in E.164 format.
 *
 * @param from A WhatsApp Business Account connected sender number in E.164 format.
 *
 * @return The verification request builder.
 */
fun VerificationRequest.Builder.whatsapp(to: String, from: String): VerificationRequest.Builder =
    addWorkflow(WhatsappWorkflow(to, from))

/**
 * Adds a WhatsApp Interactive (codeless) workflow to the verification request.
 * The user will receive a Yes / No prompt instead of a PIN code.
 *
 * @param to The recipient's phone number in E.164 format.
 *
 * @param from A WhatsApp Business Account connected sender number in E.164 format.
 *
 * @return The verification request builder.
 */
fun VerificationRequest.Builder.whatsappCodeless(to: String, from: String): VerificationRequest.Builder =
    addWorkflow(WhatsappCodelessWorkflow(to, from))
