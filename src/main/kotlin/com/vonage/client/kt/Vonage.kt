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

class Vonage(init: VonageClient.Builder.() -> Unit) {
    private val client : VonageClient = VonageClient.builder().apply(init).build()
    val account = Account(client.accountClient)
    val application = Application(client.applicationClient)
    val conversion = Conversion(client.conversionClient)
    val messages = Messages(client.messagesClient)
    val numberInsight = NumberInsight(client.insightClient)
    val numbers = Numbers(client.numbersClient)
    val numberVerification = NumberVerification(client.numberVerificationClient)
    val redact = Redact(client.redactClient)
    val simSwap = SimSwap(client.simSwapClient)
    val sms = Sms(client.smsClient)
    val subaccounts = Subaccounts(client.subaccountsClient)
    val users = Users(client.usersClient)
    val verify = Verify(client.verify2Client)
    val verifyLegacy = VerifyLegacy(client.verifyClient)
    val video = Video(client.videoClient)
    val voice = Voice(client.voiceClient)
}

fun VonageClient.Builder.authFromEnv(): VonageClient.Builder {
    val apiKey = env("VONAGE_API_KEY")
    val apiSecret = env("VONAGE_API_SECRET")
    val signatureSecret = env("VONAGE_SIGNATURE_SECRET")
    val applicationId = env("VONAGE_APPLICATION_ID")
    val privateKeyPath = env("VONAGE_PRIVATE_KEY_PATH")
    if (apiKey != null) apiKey(apiKey)
    if (apiSecret != null) apiSecret(apiSecret)
    if (signatureSecret != null) signatureSecret(signatureSecret)
    if (applicationId != null) applicationId(applicationId)
    if (privateKeyPath != null) privateKeyPath(privateKeyPath)
    return this
}

fun VonageClient.Builder.httpConfig(init: HttpConfig.Builder.() -> Unit): VonageClient.Builder =
    httpConfig(HttpConfig.builder().apply(init).build())

private fun env(variable: String) : String? = System.getenv(variable)
