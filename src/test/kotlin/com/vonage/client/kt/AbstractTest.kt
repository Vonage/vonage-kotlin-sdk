package com.vonage.client.kt

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class AbstractTest {
    val port = 8081
    val wiremock: WireMockServer = WireMockServer(
        options().port(port).notifier(ConsoleNotifier(false))
    )

    val vonageClient = Vonage {
        apiKey("a1b2c3d4")
        apiSecret("1234567890abcdef")
        applicationId("00000000-0000-4000-8000-000000000000")
        privateKeyPath("src/test/resources/com/vonage/client/kt/application_key")
        httpConfig {
            baseUri("http://localhost:$port")
        }
    }

    @BeforeEach
    fun setUp() {
        wiremock.start()
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }
}