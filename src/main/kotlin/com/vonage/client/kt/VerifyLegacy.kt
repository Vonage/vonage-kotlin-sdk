package com.vonage.client.kt

import com.vonage.client.verify.*

class VerifyLegacy(private val verifyClient: VerifyClient) {

    fun verify(number: String, brand: String, properties: (VerifyRequest.Builder.() -> Unit) = {}): VerifyResponse =
        verifyClient.verify(VerifyRequest.builder(number, brand).apply(properties).build())

    fun psd2Verify(number: String, amount: Double, payee: String,
                   properties: (Psd2Request.Builder.() -> Unit) = {}): VerifyResponse =
        verifyClient.psd2Verify(Psd2Request.builder(number, amount, payee).apply(properties).build())

    fun search(vararg requestIds: String): SearchVerifyResponse = verifyClient.search(*requestIds)

    fun request(requestId: String): ExistingRequest = ExistingRequest(requestId)

    fun request(response: VerifyResponse): ExistingRequest = request(response.requestId)

    inner class ExistingRequest internal constructor(private val requestId: String) {

        fun cancel(): ControlResponse = verifyClient.cancelVerification(requestId)

        fun advance(): ControlResponse = verifyClient.advanceVerification(requestId)

        fun check(code: String): CheckResponse = verifyClient.check(requestId, code)

        fun search(): SearchVerifyResponse = verifyClient.search(requestId)

        @Override
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ExistingRequest
            return requestId == other.requestId
        }

        @Override
        override fun hashCode(): Int {
            return requestId.hashCode()
        }
    }

}