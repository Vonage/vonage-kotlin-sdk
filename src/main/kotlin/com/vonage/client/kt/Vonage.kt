package org.example.com.vonage.client.kt

import com.vonage.client.HttpConfig
import com.vonage.client.VonageClient
import com.vonage.client.kt.Messages

class Vonage constructor(init: VonageClient.Builder.() -> Unit) {
    private val vonageClient : VonageClient = VonageClient.builder().apply(init).build();
    val verify = vonageClient.verify2Client
    val messages = Messages(vonageClient.messagesClient)
}

fun VonageClient.Builder.authFromEnv() : VonageClient.Builder {
    apiKey(env("VONAGE_API_KEY"))
    apiSecret(env("VONAGE_API_SECRET"))
    signatureSecret(env("VONAGE_SIGNATURE_SECRET"))
    applicationId(env("VONAGE_APPLICATION_ID"))
    privateKeyPath(env("VONAGE_PRIVATE_KEY_PATH"))
    return this
}

fun httpConfig(init: HttpConfig.Builder.() -> Unit): HttpConfig {
    return HttpConfig.builder().apply(init).build()
}

private fun env(variable : String) : String {
    return System.getenv(variable)
}

fun main() {
    val client = Vonage {
        authFromEnv()
    }
}