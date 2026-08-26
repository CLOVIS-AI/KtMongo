/*
 * Copyright (c) 2026, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.ktmongo.tests.api.scenarii.arrayfilters1

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

// First, let's create the different data types & the collection

@Serializable
enum class PaymentType {
	CREDIT_CARD_RECURRING,
	CREDIT_CARD_ONE_SHOT,
}

@Serializable
data class PaymentProviderConfig(
	val disabled: Boolean,
	val paymentType: PaymentType,
)

@Serializable
data class Customer(
	val _id: String,
	val paymentProviderConfig: List<PaymentProviderConfig>,
)

/**
 * This is a case study of an update using `$elemMatch`, `$set` and the positional filtered operator `$[<identifier>]`.
 *
 * The goal is to reproduce the following update:
 * ```javascript
 * mpm.getCollection("customers").updateMany(
 *     {
 *         paymentProviderConfig: {
 *             $elemMatch: {
 *                 disabled: false,
 *                 paymentType: "CREDIT_CARD_RECURRING"
 *             }
 *         }
 *     },
 *     {
 *         $set: {
 *             "paymentProviderConfig.$[toDisable].disabled": true
 *         }
 *     },
 *     {
 *         arrayFilters: [
 *             {
 *                 "toDisable.disabled": false,
 *                 "toDisable.paymentType": "CREDIT_CARD_RECURRING"
 *             }
 *         ]
 *     }
 * );
 * ```
 */
fun SuiteDsl.verifyScenarioArrayFilters(
	client: Prepared<MongoClient>,
) {

	val customers by client.collection<Customer>("scenario-arrayfilters-customers")

	test("Case study with arrayFilters") {
		customers().insertMany(
			Customer(
				_id = "recurring",
				paymentProviderConfig = listOf(
					PaymentProviderConfig(disabled = false, paymentType = PaymentType.CREDIT_CARD_RECURRING),
					PaymentProviderConfig(disabled = false, paymentType = PaymentType.CREDIT_CARD_ONE_SHOT), // wrong paymentType: untouched
				)
			),
			Customer(
				_id = "already-disabled",
				paymentProviderConfig = listOf(
					PaymentProviderConfig(disabled = true, paymentType = PaymentType.CREDIT_CARD_RECURRING), // already disabled: doesn't match the filter
				)
			),
			Customer(
				_id = "one-shot-only",
				paymentProviderConfig = listOf(
					PaymentProviderConfig(disabled = false, paymentType = PaymentType.CREDIT_CARD_ONE_SHOT), // wrong paymentType: untouched
				)
			),
		)

		val expected = listOf(
			Customer(
				_id = "already-disabled",
				paymentProviderConfig = listOf(
					PaymentProviderConfig(disabled = true, paymentType = PaymentType.CREDIT_CARD_RECURRING),
				)
			),
			Customer(
				_id = "one-shot-only",
				paymentProviderConfig = listOf(
					PaymentProviderConfig(disabled = false, paymentType = PaymentType.CREDIT_CARD_ONE_SHOT),
				)
			),
			Customer(
				_id = "recurring",
				paymentProviderConfig = listOf(
					PaymentProviderConfig(disabled = true, paymentType = PaymentType.CREDIT_CARD_RECURRING), // disabled by the update
					PaymentProviderConfig(disabled = false, paymentType = PaymentType.CREDIT_CARD_ONE_SHOT),
				)
			),
		)

		/**
		 * ```javascript
		 * mpm.getCollection("customers").updateMany(
		 *     {
		 *         paymentProviderConfig: {
		 *             $elemMatch: {
		 *                 disabled: false,
		 *                 paymentType: "CREDIT_CARD_RECURRING"
		 *             }
		 *         }
		 *     },
		 *     {
		 *         $set: {
		 *             "paymentProviderConfig.$[toDisable].disabled": true
		 *         }
		 *     },
		 *     {
		 *         arrayFilters: [
		 *             {
		 *                 "toDisable.disabled": false,
		 *                 "toDisable.paymentType": "CREDIT_CARD_RECURRING"
		 *             }
		 *         ]
		 *     }
		 * );
		 * ```
		 */
		customers().filter {
			Customer::paymentProviderConfig.any {
				PaymentProviderConfig::disabled eq false
				PaymentProviderConfig::paymentType eq PaymentType.CREDIT_CARD_RECURRING
			}
		}.updateMany {
			Customer::paymentProviderConfig.filter("toDisable") {
				it / PaymentProviderConfig::disabled eq false
				it / PaymentProviderConfig::paymentType eq PaymentType.CREDIT_CARD_RECURRING
			} / PaymentProviderConfig::disabled set true
		}

		val results = customers().find().toList().sortedBy { it._id }
		println("Results: $results")

		check(expected == results)
	}

}
