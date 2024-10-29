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

import com.vonage.client.HttpConfig
import com.vonage.client.VonageClient

const val VONAGE_KOTLIN_SDK_VERSION = "1.1.0"
private const val SDK_USER_AGENT = "vonage-kotlin-sdk/$VONAGE_KOTLIN_SDK_VERSION"

/**
 * Entry point for the SDK. This class provides access to all the Vonage APIs via its properties.
 * The constructor takes a lambda that configures the client. At a minimum, you must provide
 * your account credentials (API key and secret) and/or application ID and private key depending
 * on which APIs you plan to use. It is recommended that you set the parameters for both
 * authentication methods to maximise compatibility.
 *
 * Every sub-client for interacting with each API is exposed as a property of this class. Those classes
 * document their accepted / preferred authentication type(s). For JWT authentication, you will need to
 * provide the application ID and private key path. For API key and secret authentication, you will need
 * to provide the API key and secret. These are specified on the [VonageClient.Builder] when initialising
 * this class using the provided lambda function.
 *
 * @param config The configuration lambda, where you provide your Vonage account credentials.
 */
class Vonage(config: VonageClient.Builder.() -> Unit) {
    private val client : VonageClient = VonageClient.builder().httpConfig{}.apply(config).build()

    /**
     * Access to the Vonage Account API.
     *
     * @return The [Account] client.
     */
    val account = Account(client.accountClient)

    /**
     * Access to the Vonage Application API.
     *
     * @return The [Application] client.
     */
    val application = Application(client.applicationClient)

    /**
     * Access to the Vonage Conversion API.
     *
     * @return The [Conversion] client.
     */
    val conversion = Conversion(client.conversionClient)

    /**
     * Access to the Vonage Messages API.
     *
     * @return The [Messages] client.
     */
    val messages = Messages(client.messagesClient)

    /**
     * Access to the Vonage Number Insight API.
     *
     * @return The [NumberInsight] client.
     */
    val numberInsight = NumberInsight(client.insightClient)

    /**
     * Access to the Vonage Numbers API.
     *
     * @return The [Numbers] client.
     */
    val numbers = Numbers(client.numbersClient)

    /**
     * Access to the CAMARA Number Verification API.
     *
     * @return The [NumberVerification] client.
     */
    val numberVerification = NumberVerification(client.numberVerificationClient)

    /**
     * Access to the Vonage Redact API.
     *
     * @return The [Redact] client.
     */
    val redact = Redact(client.redactClient)

    /**
     * Access to the CAMARA SIM Swap API.
     *
     * @return The [SimSwap] client.
     */
    val simSwap = SimSwap(client.simSwapClient)

    /**
     * Access to the Vonage SMS API. For more channels and message types,
     * we recommend using the Messages API instead.
     *
     * @return The [Sms] client.
     */
    val sms = Sms(client.smsClient)

    /**
     * Access to the Vonage Subaccounts API. You must register explicitly to be able to use this.
     *
     * @return The [Subaccounts] client.
     */
    val subaccounts = Subaccounts(client.subaccountsClient)

    /**
     * Access to the Vonage Users API.
     *
     * @return The [Users] client.
     */
    val users = Users(client.usersClient)

    /**
     * Access to the Vonage Verify v2 API.
     *
     * @return The [Verify] client.
     */
    val verify = Verify(client.verify2Client)

    /**
     * Access to the Vonage Verify v1 API.
     *
     * @return The [VerifyLegacy] client.
     */
    val verifyLegacy = VerifyLegacy(client.verifyClient)

    /**
     * Access to the Vonage Video API.
     *
     * @return The [Video] client.
     */
    val video = Video(client.videoClient)

    /**
     * Access to the Vonage Voice API.
     *
     * @return The [Voice] client.
     */
    val voice = Voice(client.voiceClient)
}

/**
 * Use environment variables to populate authentication parameters.
 * This method will look for the following environment variables and set them on the builder if present:
 *
 * - **VONAGE_API_KEY**: Vonage account API key.
 * - **VONAGE_API_SECRET**: Vonage account API secret.
 * - **VONAGE_SIGNATURE_SECRET**: Vonage account signature secret.
 * - **VONAGE_APPLICATION_ID**: Vonage application ID.
 * - **VONAGE_PRIVATE_KEY_PATH**: Path to the private key file of the application.
 */
fun VonageClient.Builder.authFromEnv(): VonageClient.Builder {
    val apiKey = System.getenv("VONAGE_API_KEY")
    val apiSecret = System.getenv("VONAGE_API_SECRET")
    val signatureSecret = System.getenv("VONAGE_SIGNATURE_SECRET")
    val applicationId = System.getenv("VONAGE_APPLICATION_ID")
    val privateKeyPath = System.getenv("VONAGE_PRIVATE_KEY_PATH")
    if (apiKey != null) apiKey(apiKey)
    if (apiSecret != null) apiSecret(apiSecret)
    if (signatureSecret != null) signatureSecret(signatureSecret)
    if (applicationId != null) applicationId(applicationId)
    if (privateKeyPath != null) privateKeyPath(privateKeyPath)
    return this
}

/**
 * Optional HTTP configuration options for the client.
 *
 * @param init The config options lambda.
 * @return The builder.
 */
fun VonageClient.Builder.httpConfig(init: HttpConfig.Builder.() -> Unit): VonageClient.Builder =
    httpConfig(HttpConfig.builder().apply(init).appendUserAgent(SDK_USER_AGENT).build())
