package com.vonage.client.kt

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class VonageTest {

    @Test
    fun `auth from env`() {
        val vonage = Vonage { authFromEnv() }
        assertNotNull(vonage)
    }
}