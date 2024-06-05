package org.example.com.vonage.client.kt

import com.vonage.client.HttpConfig
import com.vonage.client.VonageClient
import com.vonage.client.messages.MessageRequest
import com.vonage.client.verify2.Verify2Client
import java.time.Instant
import java.util.*

class Vonage constructor(init: VonageClient.Builder.() -> Unit) {
    private val vonageClient : VonageClient = VonageClient.builder().apply(init).build();
    val verify: Verify2Client = vonageClient.verify2Client

    fun sendMessage(request: MessageRequest) : UUID {
        return vonageClient.messagesClient.sendMessage(request).messageUuid
    }

    fun simSwapDate(phoneNumber: String): Instant {
        return vonageClient.simSwapClient.retrieveSimSwapDate(phoneNumber)
    }
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