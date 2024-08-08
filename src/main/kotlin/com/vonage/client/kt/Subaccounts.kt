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

import com.vonage.client.subaccounts.*
import com.vonage.client.subaccounts.Account
import java.time.Instant

class Subaccounts internal constructor(private val client: SubaccountsClient) {

    fun listSubaccounts(): ListSubaccountsResponse = client.listSubaccounts()

    fun createSubaccount(name: String, secret: String? = null, usePrimaryAccountBalance: Boolean? = null): Account {
        val builder = CreateSubaccountRequest.builder().name(name).secret(secret)
        if (usePrimaryAccountBalance != null) {
            builder.usePrimaryAccountBalance(usePrimaryAccountBalance)
        }
        return client.createSubaccount(builder.build())
    }

    fun getSubaccount(subaccountKey: String): Account = client.getSubaccount(subaccountKey)

    fun updateSubaccount(subaccountKey: String, name: String? = null,
                         usePrimaryAccountBalance: Boolean? = null, suspend: Boolean? = null): Account {
        val builder = UpdateSubaccountRequest.builder(subaccountKey)
        if (name != null) {
            builder.name(name)
        }
        if (usePrimaryAccountBalance != null) {
            builder.usePrimaryAccountBalance(usePrimaryAccountBalance)
        }
        if (suspend != null) {
            builder.suspended(suspend)
        }
        return client.updateSubaccount(builder.build())
    }

    fun listCreditTransfers(startDate: Instant? = null, endDate: Instant? = null,
                            subaccount: String? = null): List<MoneyTransfer> =
        client.listCreditTransfers(ListTransfersFilter.builder()
            .startDate(startDate).endDate(endDate).subaccount(subaccount).build()
        )

    fun listBalanceTransfers(startDate: Instant? = null, endDate: Instant? = null,
                            subaccount: String? = null): List<MoneyTransfer> =
        client.listBalanceTransfers(ListTransfersFilter.builder()
            .startDate(startDate).endDate(endDate).subaccount(subaccount).build()
        )

    fun transferCredit(from: String, to: String, amount: Double, ref: String? = null): MoneyTransfer =
        client.transferCredit(MoneyTransfer.builder().from(from).to(to).amount(amount).reference(ref).build())

    fun transferBalance(from: String, to: String, amount: Double, ref: String? = null): MoneyTransfer =
        client.transferBalance(MoneyTransfer.builder().from(from).to(to).amount(amount).reference(ref).build())

    fun transferNumber(from: String, to: String, number: String, country: String): NumberTransfer =
        client.transferNumber(NumberTransfer.builder().from(from).to(to).number(number).country(country).build())

}
