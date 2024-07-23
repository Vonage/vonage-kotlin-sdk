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
