package com.vonage.client.kt

import com.vonage.client.sms.*
import com.vonage.client.sms.MessageStatus.*
import com.vonage.client.sms.messages.Message
import java.math.BigDecimal
import kotlin.test.*

class SmsTest : AbstractTest() {
    private val smsClient = vonage.sms
    private val sendUrl = "/sms/json"
    private val from = "Nexmo"
    private val clientRef = "my-personal-reference"
    private val accountRef = "customer1234"
    private val ttl = 900000
    private val statusReport = true
    private val callback = "https://example.com/sms-dlr"
    private val entityId = "1101456324675322134"
    private val contentId = "1107457532145798767"
    private val udhBinary = byteArrayOf(0x05, 0x00, 0x03, 0x7A, 0x02, 0x01)
    @OptIn(ExperimentalStdlibApi::class)
    private val udhHex = udhBinary.toHexString(HexFormat.UpperCase)
    private val protocolId = 127

    private fun testSuccessSingleMessage(requestParams: Map<String, Any>,
                                         invocation: () -> List<SmsSubmissionResponseMessage>) {

        val remainingBalance = "15.53590000"
        val messagePrice = "0.03330000"
        val network = "23410"

        mockPostQueryParams(sendUrl, requestParams, expectedResponseParams = mapOf(
                "message-count" to "1",
                "messages" to listOf(
                    mapOf(
                        "to" to toNumber,
                        "message-id" to smsMessageId,
                        "status" to "0",
                        "remaining-balance" to remainingBalance,
                        "message-price" to messagePrice,
                        "network" to network,
                        "client-ref" to clientRef,
                        "account-ref" to accountRef
                    )
                )
            )
        )

        val response = invocation.invoke()
        assertNotNull(response)
        assertEquals(1, response.size)
        val first = response.first()
        assertNotNull(first)
        assertEquals(toNumber, first.to)
        assertEquals(smsMessageId, first.id)
        assertEquals(OK, first.status)
        assertEquals(BigDecimal(remainingBalance), first.remainingBalance)
        assertEquals(BigDecimal(messagePrice), first.messagePrice)
        assertEquals(network, first.network)
        assertEquals(clientRef, first.clientRef)
        assertEquals(accountRef, first.accountRef)
        assertTrue(smsClient.wasSuccessfullySent(response))
    }

    private fun errorStatus(code: Int, text: String) = mapOf("status" to code, "error-text" to text)

    @Test
    fun `send regular text message success required parameters`() {
        testSuccessSingleMessage(mapOf("from" to from, "to" to toNumber, "text" to text, "type" to "unicode")) {
            smsClient.sendText(from, toNumber, text, unicode = true)
        }
    }

    @Test
    fun `send unicode text message success required parameters`() {
        testSuccessSingleMessage(mapOf("from" to from, "to" to toNumber, "text" to text, "type" to "text")) {
            smsClient.sendText(from, toNumber, text)
        }
    }

    @Test
    fun `send regular text message success all parameters`() {
        testSuccessSingleMessage(mapOf(
            "from" to from,
            "to" to toNumber,
            "text" to text,
            "type" to "text",
            "callback" to callback,
            "status-report-req" to if (statusReport) 1 else 0,
            "message-class" to 1,
            "ttl" to ttl,
            "client-ref" to clientRef,
            "entity-id" to entityId,
            "content-id" to contentId
        )) {
            smsClient.sendText(from, toNumber, text,
                unicode = false, statusReport = statusReport,
                ttl = ttl, messageClass = Message.MessageClass.CLASS_1,
                clientRef = clientRef, contentId = contentId, entityId = entityId,
                callbackUrl = callback
            )
        }
    }

    @Test
    fun `send binary message success required parameters`() {
        testSuccessSingleMessage(mapOf(
            "from" to from, "to" to toNumber, "type" to "binary",
            "body" to textHexEncoded, "udh" to udhHex.lowercase()
        )) {
            smsClient.sendBinary(from, toNumber, text.encodeToByteArray(), udhBinary)
        }
    }

    @Test
    fun `send binary message success all parameters`() {
        testSuccessSingleMessage(mapOf(
            "from" to from,
            "to" to toNumber,
            "body" to textHexEncoded,
            "type" to "binary",
            "udh" to udhHex.lowercase(),
            "protocol-id" to protocolId,
            "callback" to callback,
            "status-report-req" to if (statusReport) 1 else 0,
            "message-class" to 2,
            "ttl" to ttl,
            "client-ref" to clientRef,
            "entity-id" to entityId,
            "content-id" to contentId
        )) {
            smsClient.sendBinary(from, toNumber, text.encodeToByteArray(), udh = udhBinary,
                protocolId = protocolId, statusReport = statusReport, ttl = ttl,
                messageClass = Message.MessageClass.CLASS_2, clientRef = clientRef,
                contentId = contentId, entityId = entityId, callbackUrl = callback
            )
        }
    }

    @Test
    fun `send text message all statuses`() {
        val expectedRequestParams = mapOf(
            "from" to from, "to" to toNumber,
            "text" to text, "type" to "text",
        )
        val successMap = mapOf("status" to "0")

        mockPostQueryParams(sendUrl, expectedRequestParams, expectedResponseParams = mapOf(
                "message-count" to "2147483647",
                "messages" to listOf(
                    successMap, successMap, successMap, successMap,
                    errorStatus(1, "Throttled."),
                    errorStatus(2, "Missing Parameters."),
                    errorStatus(3, "Invalid Parameters."),
                    errorStatus(4, "Invalid Credentials."),
                    errorStatus(5, "Internal Error."),
                    errorStatus(6, "Invalid Message."),
                    errorStatus(7, "Number Barred."),
                    errorStatus(8, "Partner Account Barred."),
                    errorStatus(9, "Partner Quota Violation."),
                    errorStatus(10, "Too Many Existing Binds."),
                    errorStatus(11, "Account Not Enabled For HTTP."),
                    errorStatus(12, "Message Too Long."),
                    errorStatus(14, "Invalid Signature."),
                    errorStatus(15, "Invalid Sender Address."),
                    //errorStatus(17, "Message Blocked by Provider."),
                    errorStatus(22, "Invalid Network Code."),
                    errorStatus(23, "Invalid Callback Url."),
                    errorStatus(29, "Non-Whitelisted Destination."),
                    errorStatus(32, "Signature And API Secret Disallowed."),
                    errorStatus(33, "Number De-activated."), successMap
                )
            )
        )
        val response = smsClient.sendText(from, toNumber, text)
        assertNotNull(response)
        assertFalse(smsClient.wasSuccessfullySent(response))

        assertEquals(24, response.size)
        var offset = 0
        assertEquals(OK, response[offset].status)
        assertEquals(OK, response[++offset].status)
        assertEquals(OK, response[++offset].status)
        assertEquals(OK, response[++offset].status)
        assertEquals(THROTTLED, response[++offset].status)
        assertEquals("Throttled.", response[offset].errorText)
        assertEquals(MISSING_PARAMS, response[++offset].status)
        assertEquals(INVALID_PARAMS, response[++offset].status)
        assertEquals(INVALID_CREDENTIALS, response[++offset].status)
        assertEquals(INTERNAL_ERROR, response[++offset].status)
        assertEquals(INVALID_MESSAGE, response[++offset].status)
        assertEquals(NUMBER_BARRED, response[++offset].status)
        assertEquals(PARTNER_ACCOUNT_BARRED, response[++offset].status)
        assertEquals(PARTNER_QUOTA_EXCEEDED, response[++offset].status)
        assertEquals(TOO_MANY_BINDS, response[++offset].status)
        assertEquals(ACCOUNT_NOT_HTTP, response[++offset].status)
        assertEquals(MESSAGE_TOO_LONG, response[++offset].status)
        assertEquals(INVALID_SIGNATURE, response[++offset].status)
        assertEquals(INVALID_FROM_ADDRESS, response[++offset].status)
        assertEquals(INVALID_NETWORK_CODE, response[++offset].status)
        assertEquals(INVALID_CALLBACK, response[++offset].status)
        assertEquals(NON_WHITELISTED_DESTINATION, response[++offset].status)
        assertEquals(SIGNATURE_API_SECRET_DISALLOWED, response[++offset].status)
        assertEquals(NUMBER_DEACTIVATED, response[++offset].status)
        assertEquals("Number De-activated.", response[offset].errorText)
        assertEquals(OK, response[++offset].status)
    }
}