package com.vonage.client.kt

import com.vonage.client.HttpConfig
import com.vonage.client.VonageClient

class Vonage constructor(init: VonageClient.Builder.() -> Unit) {
    private val vonageClient : VonageClient = VonageClient.builder().apply(init).build();
    val messages = Messages(vonageClient.messagesClient)
    val verify = Verify(vonageClient.verify2Client)
    val voice = Voice(vonageClient.voiceClient)
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

fun VonageClient.Builder.httpConfig(init: HttpConfig.Builder.() -> Unit): VonageClient.Builder {
    return this.httpConfig(HttpConfig.builder().apply(init).build())
}

private fun env(variable: String) : String? {
    return System.getenv(variable)
}