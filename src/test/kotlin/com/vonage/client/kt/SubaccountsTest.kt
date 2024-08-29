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

import com.vonage.client.common.HttpMethod
import kotlin.test.*
import com.vonage.client.subaccounts.*
import com.vonage.client.subaccounts.Account
import java.math.BigDecimal
import java.time.temporal.ChronoUnit

class SubaccountsTest : AbstractTest() {
    private val client = vonage.subaccounts
    private val authType = AuthType.API_KEY_SECRET_HEADER
    private val existingSubaccount = client.subaccount(apiKey2)
    private val baseUrl = "/accounts/$apiKey"
    private val subaccountsUrl = "$baseUrl/subaccounts"
    private val existingSubUrl = "$subaccountsUrl/$apiKey2"
    private val name = "Subaccount department A"
    private val primaryName = "Primary Account"
    private val balance = 93.26
    private val usePrimary = false
    private val suspended = true
    private val creditLimit = -100.0
    private val amount = 48.75
    private val transferId = "07b5-46e1-a527-85530e625800"
    private val reference = "This gets added to the audit log"
    private val transferFrom = apiKey
    private val transferTo = apiKey2
    private val sampleSubaccountMap = mapOf(
        "secret" to secret,
        "api_key" to apiKey2,
        "name" to name,
        "primary_account_api_key" to apiKey,
        "use_primary_account_balance" to usePrimary,
        "created_at" to timestampStr,
        "suspended" to suspended,
        "balance" to balance,
        "credit_limit" to creditLimit
    )

    private fun assertEqualsSampleSubaccount(parsed: Account) {
        assertNotNull(parsed)
        assertEquals(secret, parsed.secret)
        assertEquals(existingSubaccount.key, parsed.apiKey)
        assertEquals(name, parsed.name)
        assertEquals(apiKey, parsed.primaryAccountApiKey)
        assertEquals(usePrimary, parsed.usePrimaryAccountBalance)
        assertEquals(timestamp, parsed.createdAt)
        assertEquals(suspended, parsed.suspended)
        assertEquals(BigDecimal.valueOf(balance), parsed.balance)
        assertEquals(BigDecimal.valueOf(creditLimit), parsed.creditLimit)
    }

    private fun assertCreateSubaccount(additionalParams: Map<String, Any>, invocation: Subaccounts.() -> Account) {
        mockPost(
            expectedUrl = subaccountsUrl, authType = authType,
            expectedRequestParams = mapOf("name" to name) + additionalParams,
            expectedResponseParams = sampleSubaccountMap
        )
        assertEqualsSampleSubaccount(invocation.invoke(client))
        assert401ApiResponseException<SubaccountsResponseException>(subaccountsUrl, HttpMethod.POST) {
            invocation.invoke(client)
        }
    }

    private enum class TransferType {
        CREDIT, BALANCE;

        @Override
        override fun toString(): String {
            return name.lowercase()
        }
    }

    private fun TransferType.getTransferUrl(): String = "$baseUrl/${toString()}-transfers"

    private fun assertTransfer(type: TransferType, includeRef: Boolean, invocation: Subaccounts.() -> MoneyTransfer) {
        val url = type.getTransferUrl()
        val baseTransferParams = mapOf(
            "amount" to amount,
            "from" to transferFrom,
            "to" to transferTo
        )

        mockPost(
            expectedUrl = url, authType = authType,
            expectedRequestParams = baseTransferParams +
                    if (includeRef) mapOf("reference" to reference) else mapOf(),
            expectedResponseParams = mapOf(
                "${type}_transfer_id" to transferId,
                "amount" to amount,
                "from" to transferFrom,
                "to" to transferTo,
                "reference" to reference,
                "created_at" to timestampStr
            )
        )

        val response = invocation.invoke(client)
        assertNotNull(response)
        assertEquals(BigDecimal.valueOf(amount), response.amount)
        assertEquals(transferFrom, response.from)
        assertEquals(transferTo, response.to)
        assertEquals(reference, response.reference)
        assertEquals(timestamp, response.createdAt)
        assertEquals(reference, response.reference)

        assert401ApiResponseException<SubaccountsResponseException>(url, HttpMethod.POST) {
            invocation.invoke(client)
        }
    }

    private fun assertListTransfers(type: TransferType, filters: Boolean, invocation: Subaccounts.() -> List<MoneyTransfer>) {
        val url = type.getTransferUrl()
        mockGet(
            expectedUrl = url, authType = authType,
            expectedQueryParams = if (filters) mapOf(
                "start_date" to timestamp.truncatedTo(ChronoUnit.SECONDS),
                "end_date" to timestamp2.truncatedTo(ChronoUnit.SECONDS),
                "subaccount" to apiKey2
            ) else mapOf(),
            expectedResponseParams = mapOf(
                "_embedded" to mapOf(
                    "${type}_transfers" to listOf(
                        mapOf(
                            "${type}_transfer_id" to transferId,
                            "amount" to amount,
                            "from" to transferFrom,
                            "to" to transferTo,
                            "reference" to reference,
                            "created_at" to timestampStr
                        ),
                        mapOf()
                    )
                )
            )
        )
        val response = invocation.invoke(client)
        assertNotNull(response)
        assertEquals(2, response.size)
        val main = response[0]
        assertNotNull(main)
        assertEquals(BigDecimal.valueOf(amount), main.amount)
        assertEquals(transferFrom, main.from)
        assertEquals(transferTo, main.to)
        assertEquals(reference, main.reference)
        assertEquals(timestamp, main.createdAt)
        val blank = response[1]
        assertNotNull(blank)
        assertNull(blank.amount)
        assertNull(blank.from)
        assertNull(blank.to)
        assertNull(blank.reference)
        assertNull(blank.createdAt)

        assert401ApiResponseException<SubaccountsResponseException>(url, HttpMethod.GET) {
            invocation.invoke(client)
        }
    }

    @Test
    fun `list subaccounts`() {
        val primaryBalance = 350.10
        val primaryCreditLimit = 123.45
        mockGet(
            expectedUrl = subaccountsUrl, authType = authType,
            expectedResponseParams = mapOf(
                "_embedded" to mapOf(
                    "primary_account" to mapOf(
                        "api_key" to apiKey,
                        "name" to primaryName,
                        "primary_account_api_key" to apiKey,
                        "use_primary_account_balance" to true,
                        "created_at" to timestamp2Str,
                        "suspended" to false,
                        "balance" to primaryBalance,
                        "credit_limit" to primaryCreditLimit
                    ),
                    "subaccounts" to listOf<Map<String, Any>>(
                        sampleSubaccountMap,
                        mapOf()
                    )
                )
            )
        )
        val response = client.listSubaccounts()
        assertNotNull(response)
        val primary = response.primaryAccount
        assertNotNull(primary)
        assertEquals(apiKey, primary.apiKey)
        assertEquals(primaryName, primary.name)
        assertEquals(apiKey, primary.primaryAccountApiKey)
        assertTrue(primary.usePrimaryAccountBalance)
        assertEquals(timestamp2, primary.createdAt)
        assertFalse(primary.suspended)
        assertEquals(BigDecimal.valueOf(primaryBalance), primary.balance)
        assertEquals(BigDecimal.valueOf(primaryCreditLimit), primary.creditLimit)

        val subaccounts = response.subaccounts
        assertEquals(2, subaccounts.size)
        assertEqualsSampleSubaccount(subaccounts[0])
        val blank = subaccounts[1]
        assertNotNull(blank)
        assertNull(blank.secret)
        assertNull(blank.apiKey)
        assertNull(blank.createdAt)
        assertNull(blank.suspended)
        assertNull(blank.balance)
        assertNull(blank.creditLimit)
        assert401ApiResponseException<SubaccountsResponseException>(subaccountsUrl, HttpMethod.GET) {
            client.listSubaccounts()
        }
    }

    @Test
    fun `create subaccount required parameters`() {
        assertCreateSubaccount(mapOf()) { createSubaccount(name) }
    }

    @Test
    fun `create subaccount all parameters`() {
        assertCreateSubaccount(mapOf(
            "secret" to secret,
            "use_primary_account_balance" to usePrimary
        )) {
            createSubaccount(name, secret, usePrimary)
        }
    }

    @Test
    fun `get subaccount`() {
        mockGet(
            expectedUrl = existingSubUrl, authType = authType,
            expectedResponseParams = sampleSubaccountMap
        )
        assertEqualsSampleSubaccount(existingSubaccount.get())
        assert401ApiResponseException<SubaccountsResponseException>(existingSubUrl, HttpMethod.GET) {
            existingSubaccount.get()
        }
    }

    @Test
    fun `update subaccount`() {
        mockPatch(
            expectedUrl = existingSubUrl, authType = authType,
            expectedRequestParams = mapOf(
                "name" to name,
                "use_primary_account_balance" to usePrimary
            ),
            expectedResponseParams = sampleSubaccountMap
        )
        assertEqualsSampleSubaccount(existingSubaccount.update(name, usePrimary))
        assert401ApiResponseException<SubaccountsResponseException>(existingSubUrl, HttpMethod.PATCH) {
            existingSubaccount.update(usePrimaryAccountBalance = usePrimary)
        }
    }

    @Test
    fun `suspend subaccount`() {
        mockPatch(
            expectedUrl = existingSubUrl, authType = authType,
            expectedRequestParams = mapOf("suspended" to suspended),
            expectedResponseParams = sampleSubaccountMap
        )
        assertEqualsSampleSubaccount(existingSubaccount.suspended(suspended))
        assert401ApiResponseException<SubaccountsResponseException>(existingSubUrl, HttpMethod.PATCH) {
            existingSubaccount.suspended(!suspended)
        }
    }

    @Test
    fun `list credit transfers no parameters`() {
        assertListTransfers(TransferType.CREDIT, false) {
            listCreditTransfers()
        }
    }

    @Test
    fun `list credit transfers all parameters`() {
        assertListTransfers(TransferType.CREDIT, true) {
            listCreditTransfers(timestamp, timestamp2, apiKey2)
        }
    }

    @Test
    fun `list balance transfers no parameters`() {
        assertListTransfers(TransferType.BALANCE, false) {
            listBalanceTransfers()
        }
    }

    @Test
    fun `list balance transfers all parameters`() {
        assertListTransfers(TransferType.BALANCE, true) {
            listBalanceTransfers(timestamp, timestamp2, apiKey2)
        }
    }

    @Test
    fun `transfer credit no reference`() {
        assertTransfer(TransferType.CREDIT, false) {
            transferCredit(transferFrom, transferTo, amount)
        }
    }

    @Test
    fun `transfer credit with reference`() {
        assertTransfer(TransferType.CREDIT, true) {
            transferCredit(transferFrom, transferTo, amount, reference)
        }
    }

    @Test
    fun `transfer balance no reference`() {
        assertTransfer(TransferType.BALANCE, false) {
            transferBalance(transferFrom, transferTo, amount)
        }
    }

    @Test
    fun `transfer balance with reference`() {
        assertTransfer(TransferType.BALANCE, true) {
            transferBalance(transferFrom, transferTo, amount, reference)
        }
    }

    @Test
    fun `transfer number`() {
        val url = "$baseUrl/transfer-number"
        val params = mapOf(
            "number" to toNumber,
            "country" to country,
            "from" to transferFrom,
            "to" to transferTo
        )
        mockPost(
            expectedUrl = url, authType = authType,
            expectedRequestParams = params, expectedResponseParams = params
        )
        val response = client.transferNumber(transferFrom, transferTo, toNumber, country)
        assertNotNull(response)
        assertEquals(transferFrom, response.from)
        assertEquals(transferTo, response.to)
        assertEquals(toNumber, response.number)
        assertEquals(country, response.country)

        assert401ApiResponseException<SubaccountsResponseException>(url, HttpMethod.POST) {
            client.transferNumber(transferFrom, transferTo, toNumber, country)
        }
    }
}