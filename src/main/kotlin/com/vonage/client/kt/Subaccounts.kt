/*
 *   Copyright 2025 Vonage
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

import com.vonage.client.subaccounts.*
import com.vonage.client.subaccounts.Account
import java.time.Instant

/**
 * Implementation of the [Subaccounts API](https://developer.vonage.com/en/api/subaccounts).
 *
 * *Authentication method:* API key & secret.
 */
class Subaccounts internal constructor(private val client: SubaccountsClient) {

    /**
     * Retrieve all subaccounts owned by the primary account.
     *
     * @return The primary account details and list of subaccounts associated with it.
     *
     * @throws [SubaccountsResponseException] If the subaccounts could not be retrieved.
     * This may be for the following reasons:
     *
     * - **401**: Missing or invalid credentials.
     * - **403**: Action is forbidden.
     * - **404**: The account ID provided does not exist in the system, or you do not have access.
     */
    fun listSubaccounts(): ListSubaccountsResponse = client.listSubaccounts()

    /**
     * Create a new subaccount.
     *
     * @param name Name of the subaccount.
     * @param secret (OPTIONAL) Secret for the subaccount. If not provided, one will be generated.
     * @param usePrimaryAccountBalance (OPTIONAL) Whether to use the primary account's balance.
     *
     * @return The newly created subaccount details.
     *
     * @throws [SubaccountsResponseException] If the subaccount could not be created.
     * This may be for the following reasons:
     *
     * - **401**: Missing or invalid credentials.
     * - **403**: Action is forbidden.
     * - **404**: The account ID provided does not exist in the system, or you do not have access.
     * - **422**: Validation error.
     */
    fun createSubaccount(name: String, secret: String? = null, usePrimaryAccountBalance: Boolean? = null): Account {
        val builder = CreateSubaccountRequest.builder().name(name).secret(secret)
        if (usePrimaryAccountBalance != null) {
            builder.usePrimaryAccountBalance(usePrimaryAccountBalance)
        }
        return client.createSubaccount(builder.build())
    }

    /**
     * Call this method to work with an existing subaccount.
     *
     * @param subaccountKey API key of the subaccount to work with.
     *
     * @return An [ExistingSubaccount] object with methods to interact with the subaccount.
     */
    fun subaccount(subaccountKey: String): ExistingSubaccount = ExistingSubaccount(subaccountKey)

    /**
     * Class for working with an existing subaccount.
     *
     * @property id The subaccount's API key.
     */
    inner class ExistingSubaccount internal constructor(key: String): ExistingResource(key) {

        /**
         * Retrieves the subaccount details.
         *
         * @return The subaccount details.
         *
         * @throws [SubaccountsResponseException] If the subaccount details could not be retrieved.
         * This may be for the following reasons:
         *
         * - **401**: Missing or invalid credentials.
         * - **403**: Action is forbidden.
         * - **404**: The account ID provided does not exist in the system, or you do not have access.
         */
        fun get(): Account = client.getSubaccount(id)

        /**
         * Suspends or unsuspends the subaccount. Note that suspension will not delete it;
         * only prevent it from being used to make API calls.
         *
         * @param suspend Set to `true` to suspend the subaccount or `false` to restore it.
         *
         * @return The updated subaccount details.
         *
         * @throws [SubaccountsResponseException] If the subaccount could not be suspended.
         * This may be for the following reasons:
         *
         * - **401**: Missing or invalid credentials.
         * - **403**: Action is forbidden.
         * - **404**: The account ID provided does not exist in the system, or you do not have access.
         * - **422**: Validation error.
         */
        fun suspended(suspend: Boolean): Account =
            client.updateSubaccount(UpdateSubaccountRequest.builder(id).suspended(suspend).build())

        /**
         * Updates the subaccount details. At least one field must be provided.
         *
         * @param name (OPTIONAL) New name for the subaccount.
         * @param usePrimaryAccountBalance (OPTIONAL) Whether to use the primary account's balance.
         *
         * @return The updated subaccount details.
         *
         * @throws [SubaccountsResponseException] If the subaccount details could not be updated.
         * This may be for the following reasons:
         *
         * - **401**: Missing or invalid credentials.
         * - **403**: Action is forbidden.
         * - **404**: The account ID provided does not exist in the system, or you do not have access.
         * - **422**: Validation error.
         */
        fun update(name: String? = null, usePrimaryAccountBalance: Boolean? = null): Account {
            val builder = UpdateSubaccountRequest.builder(id).name(name)
            if (usePrimaryAccountBalance != null) {
                builder.usePrimaryAccountBalance(usePrimaryAccountBalance)
            }
            return client.updateSubaccount(builder.build())
        }
    }

    /**
     * Retrieve credit limit transfers that have occurred for the primary account within a specified time period.
     *
     * @param startDate (OPTIONAL) The start of the time period to retrieve transfers for.
     * @param endDate (OPTIONAL) The end of the time period to retrieve transfers for.
     * @param subaccount (OPTIONAL) The subaccount API key to retrieve transfers for.
     *
     * @return The list of credit transfers that have occurred which match the filter criteria.
     *
     * @throws [SubaccountsResponseException] If the transfers could not be retrieved.
     * This may be for the following reasons:
     *
     * - **401**: Missing or invalid credentials.
     * - **403**: Action is forbidden.
     * - **404**: The account ID provided does not exist in the system, or you do not have access.
     */
    fun listCreditTransfers(startDate: Instant? = null, endDate: Instant? = null,
                            subaccount: String? = null): List<MoneyTransfer> =
        client.listCreditTransfers(ListTransfersFilter.builder()
            .startDate(startDate).endDate(endDate).subaccount(subaccount).build()
        )

    /**
     * Retrieve balance transfers that have occurred for the primary account within a specified time period.
     *
     * @param startDate (OPTIONAL) The start of the time period to retrieve transfers for.
     * @param endDate (OPTIONAL) The end of the time period to retrieve transfers for.
     * @param subaccount (OPTIONAL) The subaccount API key to retrieve transfers for.
     *
     * @return The list of balance transfers that have occurred which match the filter criteria.
     *
     * @throws [SubaccountsResponseException] If the transfers could not be retrieved.
     * This may be for the following reasons:
     *
     * - **401**: Missing or invalid credentials.
     * - **403**: Action is forbidden.
     * - **404**: The account ID provided does not exist in the system, or you do not have access.
     */
    fun listBalanceTransfers(startDate: Instant? = null, endDate: Instant? = null,
                            subaccount: String? = null): List<MoneyTransfer> =
        client.listBalanceTransfers(ListTransfersFilter.builder()
            .startDate(startDate).endDate(endDate).subaccount(subaccount).build()
        )

    /**
     * Transfer credit limit between (sub)accounts.
     *
     * @param from The API key of the account to transfer credit from.
     * @param to The API key of the subaccount to transfer credit to.
     * @param amount The monetary amount of credit to transfer in Euros.
     * @param ref (OPTIONAL) A reference to associate with the transfer.
     *
     * @return Details of the credit transfer.
     *
     * @throws [SubaccountsResponseException] If the credit could not be transferred.
     * This may be for the following reasons:
     *
     * - **401**: Missing or invalid credentials.
     * - **403**: Action is forbidden.
     * - **404**: The account ID provided does not exist in the system, or you do not have access.
     * - **422**: Validation error.
     */
    fun transferCredit(from: String, to: String, amount: Double, ref: String? = null): MoneyTransfer =
        client.transferCredit(MoneyTransfer.builder().from(from).to(to).amount(amount).reference(ref).build())

    /**
     * Transfer monetary balance between (sub)accounts.
     *
     * @param from The API key of the account to transfer money from.
     * @param to The API key of the subaccount to transfer money to.
     * @param amount The monetary amount to transfer in Euros.
     * @param ref (OPTIONAL) A reference to associate with the transfer.
     *
     * @return Details of the balance transfer.
     *
     * @throws [SubaccountsResponseException] If the balance could not be transferred.
     * This may be for the following reasons:
     *
     * - **401**: Missing or invalid credentials.
     * - **403**: Action is forbidden.
     * - **404**: The account ID provided does not exist in the system, or you do not have access.
     * - **422**: Validation error.
     */
    fun transferBalance(from: String, to: String, amount: Double, ref: String? = null): MoneyTransfer =
        client.transferBalance(MoneyTransfer.builder().from(from).to(to).amount(amount).reference(ref).build())

    /**
     * Transfer a number from one account to another.
     *
     * @param from API key of the account to transfer the number from.
     * @param to API key of the account to transfer the number to.
     * @param number MSISDN of the number to transfer, in E.164 format.
     * @param country Country the number is registered in, in ISO 3166-1 alpha-2 format.
     *
     * @return Details of the number transfer.
     *
     * @throws [SubaccountsResponseException] If the number could not be transferred.
     * This may be for the following reasons:
     *
     * - **401**: Missing or invalid credentials.
     * - **403**: Action is forbidden.
     * - **404**: The account ID provided does not exist in the system, or you do not have access.
     * - **409**: The number is already associated with the account you are trying to transfer it to.
     * - **422**: Validation error.
     */
    fun transferNumber(from: String, to: String, number: String, country: String): NumberTransfer =
        client.transferNumber(NumberTransfer.builder().from(from).to(to).number(number).country(country).build())

}
