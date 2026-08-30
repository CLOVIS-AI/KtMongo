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

package opensavvy.ktmongo.tests.api.scenarii.unwind1

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.api.toList
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

@Serializable
data class Subscription(
	val subscriptionDefRef: ObjectId,
	val targetRef: ObjectId,
)

@Serializable
data class CustomerAccount(
	val _id: ObjectId,
	val subscriptions: List<Subscription>,
)

@Serializable
data class AdminAccount(
	val _id: ObjectId,
	val adminRef: ObjectId,
	val customerAccountRefs: List<ObjectId>,
)

@Serializable
data class AdminAccountUnwoundRef(
	val _id: ObjectId,
	val adminRef: ObjectId,
	val customerAccountRefs: ObjectId,
	val customerAccountsDoc: List<CustomerAccount> = emptyList(),
)

@Serializable
data class CustomerAccountUnwoundSubscription(
	val _id: ObjectId,
	val subscriptions: Subscription,
)

@Serializable
data class AdminAccountUnwoundCustomer(
	val _id: ObjectId,
	val adminRef: ObjectId,
	val customerAccountRefs: ObjectId,
	val customerAccountsDoc: CustomerAccount,
)

@Serializable
data class AdminAccountUnwoundSubscription(
	val _id: ObjectId,
	val adminRef: ObjectId,
	val customerAccountRefs: ObjectId,
	val customerAccountsDoc: CustomerAccountUnwoundSubscription,
)

@Serializable
data class GroupId(
	val tradeAgreementRef: ObjectId,
	val operatorRef: ObjectId,
)

@Serializable
data class GroupedSubscription(
	val _id: GroupId,
	val count: Int,
)

@Serializable
data class ActiveSubscription(
	val tradeAgreementRef: ObjectId,
	val operatorRef: ObjectId,
	val count: Int,
)

/**
 * Case study of a complex aggregation pipeline chaining multiple `$unwind`, `$lookup`, `$match`, `$group`, and `$project` stages.
 */
fun SuiteDsl.verifyScenarioUnwind(
	client: Prepared<MongoClient>,
) {
	val adminAccounts by client.collection<AdminAccount>("scenario-unwind1-admin-accounts")
	val customerAccounts by client.collection<CustomerAccount>("scenario-unwind1-customer-accounts")

	test("Case study with multiple unwinds, lookup, match, group, and project") {
		val targetSubscriptionDefId = adminAccounts().newId()
		val otherSubscriptionDefId = adminAccounts().newId()

		val operator1 = adminAccounts().newId()
		val operator2 = adminAccounts().newId()
		val operator3 = adminAccounts().newId()

		val tradeAgreement1 = adminAccounts().newId()
		val tradeAgreement2 = adminAccounts().newId()
		val tradeAgreement3 = adminAccounts().newId()

		val ca1Id = customerAccounts().newId()
		val ca2Id = customerAccounts().newId()
		val ca3Id = customerAccounts().newId()
		val ca4Id = customerAccounts().newId()
		val ca5Id = customerAccounts().newId()
		val caUnreferencedId = customerAccounts().newId()

		customerAccounts().insertMany(
			CustomerAccount(
				_id = ca1Id,
				subscriptions = listOf(
					Subscription(subscriptionDefRef = targetSubscriptionDefId, targetRef = tradeAgreement1),
					Subscription(subscriptionDefRef = otherSubscriptionDefId, targetRef = tradeAgreement1), // Excluded: non-matching sub def
					Subscription(subscriptionDefRef = targetSubscriptionDefId, targetRef = tradeAgreement2),
				),
			),
			CustomerAccount(
				_id = ca2Id,
				subscriptions = listOf(
					Subscription(subscriptionDefRef = targetSubscriptionDefId, targetRef = tradeAgreement1),
				),
			),
			CustomerAccount(
				_id = ca3Id,
				subscriptions = listOf(
					Subscription(subscriptionDefRef = targetSubscriptionDefId, targetRef = tradeAgreement1),
				),
			),
			CustomerAccount(
				_id = ca4Id,
				subscriptions = listOf(
					Subscription(subscriptionDefRef = targetSubscriptionDefId, targetRef = tradeAgreement1),
					Subscription(subscriptionDefRef = targetSubscriptionDefId, targetRef = tradeAgreement2),
				),
			),
			CustomerAccount(
				_id = ca5Id,
				subscriptions = listOf(
					Subscription(subscriptionDefRef = otherSubscriptionDefId, targetRef = tradeAgreement3), // Excluded: non-matching sub def
				),
			),
			CustomerAccount(
				_id = caUnreferencedId,
				subscriptions = listOf(
					Subscription(subscriptionDefRef = targetSubscriptionDefId, targetRef = tradeAgreement1), // Excluded: not referenced by FO accounts
				),
			),
		)

		adminAccounts().insertMany(
			AdminAccount(
				_id = adminAccounts().newId(),
				adminRef = operator1,
				customerAccountRefs = listOf(ca1Id, ca2Id),
			),
			AdminAccount(
				_id = adminAccounts().newId(),
				adminRef = operator1,
				customerAccountRefs = listOf(ca3Id),
			),
			AdminAccount(
				_id = adminAccounts().newId(),
				adminRef = operator2,
				customerAccountRefs = listOf(ca4Id),
			),
			AdminAccount(
				_id = adminAccounts().newId(),
				adminRef = operator3,
				customerAccountRefs = listOf(ca5Id),
			),
		)

		val customerAccountsPipeline = customerAccounts().aggregate()

		val results = adminAccounts().aggregate()
			/*
			 * { $unwind: '$customerAccountRefs' }
			 */
			.unwind<ObjectId, AdminAccountUnwoundRef> {
				array(AdminAccount::customerAccountRefs)
			}
			/*
			 * {
			 *   $lookup: {
			 *     from: 'customerAccounts',
			 *     localField: 'customerAccountRefs',
			 *     foreignField: '_id',
			 *     as: 'customerAccountsDoc'
			 *   }
			 * }
			 */
			.lookup {
				from(customerAccountsPipeline)
				on(AdminAccountUnwoundRef::customerAccountRefs, CustomerAccount::_id)
				into(AdminAccountUnwoundRef::customerAccountsDoc)
			}
			/*
			 * { $unwind: '$customerAccountsDoc' }
			 */
			.unwind<CustomerAccount, AdminAccountUnwoundCustomer> {
				array(AdminAccountUnwoundRef::customerAccountsDoc)
			}
			/*
			 * { $unwind: '$customerAccountsDoc.subscriptions' }
			 */
			.unwind<Subscription, AdminAccountUnwoundSubscription> {
				array(AdminAccountUnwoundCustomer::customerAccountsDoc / CustomerAccount::subscriptions)
			}
			/*
			 * {
			 *   $match: {
			 *     'customerAccountsDoc.subscriptions.subscriptionDefRef': targetSubscriptionDefId
			 *   }
			 * }
			 */
			.match {
				(AdminAccountUnwoundSubscription::customerAccountsDoc / CustomerAccountUnwoundSubscription::subscriptions / Subscription::subscriptionDefRef) eq targetSubscriptionDefId
			}
			/*
			 * {
			 *   $group: {
			 *     _id: {
			 *       tradeAgreementRef: '$customerAccountsDoc.subscriptions.targetRef',
			 *       operatorRef: '$adminRef'
			 *     },
			 *     count: { $sum: 1 }
			 *   }
			 * }
			 */
			.group {
				GroupedSubscription::_id / GroupId::tradeAgreementRef set (AdminAccountUnwoundSubscription::customerAccountsDoc / CustomerAccountUnwoundSubscription::subscriptions / Subscription::targetRef)
				GroupedSubscription::_id / GroupId::operatorRef set AdminAccountUnwoundSubscription::adminRef
				GroupedSubscription::count sum of(1)
			}
			/*
			 * {
			 *   $project: {
			 *     _id: 0,
			 *     tradeAgreementRef: '$_id.tradeAgreementRef',
			 *     operatorRef: '$_id.operatorRef',
			 *     count: 1
			 *   }
			 * }
			 */
			.project {
				excludeId()
				ActiveSubscription::tradeAgreementRef set (GroupedSubscription::_id / GroupId::tradeAgreementRef)
				ActiveSubscription::operatorRef set (GroupedSubscription::_id / GroupId::operatorRef)
				include(ActiveSubscription::count)
			}
			.toList()

		val expected = listOf(
			ActiveSubscription(tradeAgreementRef = tradeAgreement1, operatorRef = operator1, count = 3),
			ActiveSubscription(tradeAgreementRef = tradeAgreement2, operatorRef = operator1, count = 1),
			ActiveSubscription(tradeAgreementRef = tradeAgreement1, operatorRef = operator2, count = 1),
			ActiveSubscription(tradeAgreementRef = tradeAgreement2, operatorRef = operator2, count = 1),
		)

		check(results.toSet() == expected.toSet()) { "Expected $expected but got $results" }
		check(results.size == expected.size) { "Expected ${expected.size} results but got ${results.size}" }
	}
}
