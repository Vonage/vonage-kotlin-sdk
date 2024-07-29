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
    private val vonageClient : VonageClient = VonageClient.builder().apply(init).build();
    val messages = Messages(vonageClient.messagesClient)
    val verify = Verify(vonageClient.verify2Client)
    val voice = Voice(vonageClient.voiceClient)
    val sms = Sms(vonageClient.smsClient)
    val conversion = Conversion(vonageClient.conversionClient)
    val redact = Redact(vonageClient.redactClient)
    val verifyLegacy = VerifyLegacy(vonageClient.verifyClient)
    val numberInsight = NumberInsight(vonageClient.insightClient)
    val simSwap = SimSwap(vonageClient.simSwapClient)
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
