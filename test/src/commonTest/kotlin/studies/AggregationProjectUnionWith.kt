/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.sync.studies

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.coroutines.MongoAggregationPipeline
import opensavvy.ktmongo.coroutines.toList
import opensavvy.ktmongo.dsl.aggregation.stages.ProjectStageOperators
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.test.testCollection
import opensavvy.prepared.runner.testballoon.preparedSuite

// First, let's create the different data types & the collections

@Serializable
data class Customer(
	val _id: String,
	val name: String,
	val biography: String?,
)

@Serializable
data class Vendor(
	val _id: String,
	val name: String,
)

@Serializable
data class AuditData(
	val creationDate: Int,
	val deletionDate: Int?,
)

@Serializable
data class Invoice(
	val _id: String,
	val subject: String,
	val metadata: String,
	val customer: Customer,
	val vendor: Vendor?,
	val auditData: AuditData? = null,
	val paymentStatus: Boolean? = null,
	val acceptedStatus: Boolean? = null,
	val draft: Boolean? = null,
	val documentType: String? = null,
)

/**
 * This is a case study of a complex aggregation pipeline using $match, $set, $project, $sort, $limit and $unionWith.
 *
 * The goal is to reproduce the following aggregation pipeline:
 * ```javascript
 * const commonMatch = {
 * 	'vendor._id': "507f1f77bcf86cd799439011", 'customer._id': "507f191e810c19729de860ea",
 * }
 *
 * const commonProjection = {
 * 	_id: 1,
 * 	subject: 1,
 * 	metadata: 1,
 * 	'customer._id': 1,
 * 	'customer.name': 1,
 * };
 *
 * const commonSort = {$sort: {'customer.name': -1}};
 * const commonLimit = {$limit: 20};
 *
 * db.getCollection("drafts").aggregate([
 * 	{
 * 		$match: {
 * 			'auditData.deletionDate': {$exists: false},
 * 			...commonMatch,
 * 		},
 * 	},
 * 	{
 * 		$project: {
 * 			documentType: 1,
 * 			...commonProjection,
 * 		},
 * 	},
 * 	{
 * 		$addFields: {
 * 			draft: true,
 * 		},
 * 	},
 * 	commonSort,
 * 	commonLimit,
 * 	{
 * 		$unionWith: {
 * 			coll: 'invoiceA',
 * 			pipeline: [
 * 				{$match: commonMatch},
 * 				{$project: {paymentStatus: 1, ...commonProjection}},
 * 				{
 * 					$addFields: {
 * 						draft: false, documentType: 'A',
 * 					},
 * 				},
 * 				commonSort,
 * 				commonLimit,
 * 			],
 * 		},
 * 	},
 * 	{
 * 		$unionWith: {
 * 			coll: 'invoiceB',
 * 			pipeline: [
 * 				{$match: commonMatch},
 * 				{$project: {acceptedStatus: 1, ...commonProjection}},
 * 				{
 * 					$addFields: {
 * 						draft: false, documentType: 'B',
 * 					},
 * 				},
 * 				commonSort,
 * 				commonLimit,
 * 			],
 * 		},
 * 	}, {
 * 		$unionWith: {
 * 			coll: 'invoiceC',
 * 			pipeline: [
 * 				{$match: commonMatch},
 * 				{$project: commonProjection},
 * 				{
 * 					$addFields: {
 * 						draft: false, documentType: 'C',
 * 					},
 * 				},
 * 				commonSort,
 * 				commonLimit,
 * 			],
 * 		},
 * 	},
 * 	commonSort,
 * 	commonLimit,
 * ]);
 * ```
 */
val AggregationProjectUnionWith by preparedSuite {

	val drafts by testCollection<Invoice>("case-unionWith-drafts")
	val invoiceAs by testCollection<Invoice>("case-unionWith-invoice-a")
	val invoiceBs by testCollection<Invoice>("case-unionWith-invoice-b")
	val invoiceCs by testCollection<Invoice>("case-unionWith-invoice-c")

	/**
	 * Custom `$match` to select a specific vendor and a specific customer.
	 *
	 * ```javascript
	 * const commonMatch = {
	 * 	'vendor._id': "507f1f77bcf86cd799439011",
	 * 	'customer._id': "507f191e810c19729de860ea",
	 * }
	 * ```
	 */
	fun FilterQuery<Invoice>.commonMatch() = and {
		Invoice::vendor / Vendor::_id eq "507f1f77bcf86cd799439011"
		Invoice::customer / Customer::_id eq "507f191e810c19729de860ea"
	}

	/**
	 * Custom `$project` to remove information we don't need.
	 *
	 * ```javascript
	 * const commonProjection = {
	 * 	_id: 1,
	 * 	subject: 1,
	 * 	metadata: 1,
	 * 	'customer._id': 1,
	 * 	'customer.name': 1,
	 * };
	 * ```
	 */
	fun ProjectStageOperators<Invoice>.commonProjection() {
		include(Invoice::_id)
		include(Invoice::subject)
		include(Invoice::metadata)
		include(Invoice::customer / Customer::_id)
		include(Invoice::customer / Customer::name)
	}

	/**
	 * Custom `$sort`.
	 *
	 * ```javascript
	 * const commonSort = {$sort: {'customer.name': -1}};
	 * ```
	 */
	fun MongoAggregationPipeline<Invoice>.commonSort() = sort {
		descending(Invoice::customer / Customer::name)
	}

	/**
	 * Custom `$limit`.
	 *
	 * ```javascript
	 * const commonLimit = {$limit: 20};
	 * ```
	 */
	fun MongoAggregationPipeline<Invoice>.commonLimit() = limit(20)

	test("Case study with \$unionWith") {
		drafts().insertOne(
			Invoice(
				_id = "whatever",
				subject = "Whatever",
				metadata = "metadata",
				customer = Customer("507f191e810c19729de860ea", "Bob", "Bob's biography"),
				vendor = Vendor("507f1f77bcf86cd799439011", "Alice"),
			)
		)
		drafts().insertOne( // won't be kept because the IDs are incorrect
			Invoice(
				_id = "whatever2",
				subject = "Whatever 2",
				metadata = "metadata 2",
				customer = Customer("507f1f77bcf86cd799439011", "Alice", "Alice's biography"),
				vendor = Vendor("507f191e810c19729de860ea", "Bob"),
			)
		)
		invoiceAs().insertOne(
			Invoice(
				_id = "whatever 3",
				subject = "Whatever 3",
				metadata = "metadata",
				customer = Customer("507f191e810c19729de860ea", "Bob", "Bob's biography"),
				vendor = Vendor("507f1f77bcf86cd799439011", "Alice"),
				paymentStatus = true,
				acceptedStatus = false, // this field will be removed by the projection
			)
		)
		invoiceBs().insertOne(
			Invoice(
				_id = "whatever 4",
				subject = "Whatever 4",
				metadata = "metadata",
				customer = Customer("507f191e810c19729de860ea", "Bob", "Bob's biography"),
				vendor = Vendor("507f1f77bcf86cd799439011", "Alice"),
				paymentStatus = true, // this field will be removed by the projection
				acceptedStatus = false,
			)
		)
		invoiceCs().insertOne(
			Invoice(
				_id = "whatever 5",
				subject = "Whatever 5",
				metadata = "metadata",
				customer = Customer("507f191e810c19729de860ea", "Bob", "Bob's biography"),
				vendor = Vendor("507f1f77bcf86cd799439011", "Alice"),
				paymentStatus = true, // this field will be removed by the projection
				acceptedStatus = false, // this field will be removed by the projection
			)
		)

		val expected = listOf(
			Invoice(
				_id = "whatever",
				subject = "Whatever",
				metadata = "metadata",
				customer = Customer("507f191e810c19729de860ea", "Bob", null),
				vendor = null,
				draft = true,
			),
			Invoice(
				_id = "whatever 3",
				subject = "Whatever 3",
				metadata = "metadata",
				customer = Customer("507f191e810c19729de860ea", "Bob", null),
				vendor = null,
				paymentStatus = true,
				draft = false,
				documentType = "A",
			),
			Invoice(
				_id = "whatever 4",
				subject = "Whatever 4",
				metadata = "metadata",
				customer = Customer("507f191e810c19729de860ea", "Bob", null),
				vendor = null,
				acceptedStatus = false,
				draft = false,
				documentType = "B",
			),
			Invoice(
				_id = "whatever 5",
				subject = "Whatever 5",
				metadata = "metadata",
				customer = Customer("507f191e810c19729de860ea", "Bob", null),
				vendor = null,
				draft = false,
				documentType = "C",
			),
		)

		/**
		 * ```javascript
		 * db.getCollection("drafts").aggregate([
		 * 	{
		 * 		$match: {
		 * 			'auditData.deletionDate': {$exists: false},
		 * 			...commonMatch,
		 * 		},
		 * 	},
		 * 	{
		 * 		$project: {
		 * 			documentType: 1,
		 * 			...commonProjection,
		 * 		},
		 * 	},
		 * 	{
		 * 		$addFields: {
		 * 			draft: true,
		 * 		},
		 * 	},
		 * ```
		 */
		val selectedDrafts = drafts().aggregate()
			.match {
				(Invoice::auditData / AuditData::deletionDate).doesNotExist()
				commonMatch()
			}
			.project {
				include(Invoice::documentType)
				commonProjection()
				Invoice::draft set true // we can set fields directly within $project, but we could also use a $set stage if we wanted
			}
			.commonSort()
			.commonLimit()
		println("Selected drafts: $selectedDrafts\n\t${selectedDrafts.toList()}")

		/**
		 * ```json
		 * coll: 'invoiceA',
		 * pipeline: [
		 * 	{$match: commonMatch},
		 * 	{$project: {paymentStatus: 1, ...commonProjection}},
		 * 	{
		 * 		$addFields: {
		 * 			draft: false, documentType: 'A',
		 * 		},
		 * 	},
		 * 	commonSort,
		 * 	commonLimit,
		 * ],
		 * ```
		 */
		val selectedInvoicesA = invoiceAs().aggregate()
			.match { commonMatch() }
			.project {
				include(Invoice::paymentStatus)
				commonProjection()
				Invoice::draft set false
				Invoice::documentType set "A"
			}
			.commonSort()
			.commonLimit()
		println("Selected invoices A: $selectedInvoicesA\n\t${selectedInvoicesA.toList()}")

		/**
		 * ```javascript
		 * coll: 'invoiceB',
		 * pipeline: [
		 * 	{$match: commonMatch},
		 * 	{$project: {acceptedStatus: 1, ...commonProjection}},
		 * 	{
		 * 		$addFields: {
		 * 			draft: false, documentType: 'B',
		 * 		},
		 * 	},
		 * 	commonSort,
		 * 	commonLimit,
		 * ],
		 * ```
		 */
		val selectedInvoicesB = invoiceBs().aggregate()
			.match { commonMatch() }
			.project {
				include(Invoice::acceptedStatus)
				commonProjection()
				Invoice::draft set false
				Invoice::documentType set "B"
			}
			.commonSort()
			.commonLimit()
		println("Selected invoices B: $selectedInvoicesB\n\t${selectedInvoicesB.toList()}")

		/**
		 * ```javascript
		 * coll: 'invoiceC',
		 * pipeline: [
		 * 	{$match: commonMatch},
		 * 	{$project: commonProjection},
		 * 	{
		 * 		$addFields: {
		 * 			draft: false, documentType: 'C',
		 * 		},
		 * 	},
		 * 	commonSort,
		 * 	commonLimit,
		 * ],
		 * ```
		 */
		val selectedInvoicesC = invoiceCs().aggregate()
			.match { commonMatch() }
			.project {
				commonProjection()
				Invoice::draft set false
				Invoice::documentType set "C"
			}
			.commonSort()
			.commonLimit()
		println("Selected invoices C: $selectedInvoicesC\n\t${selectedInvoicesC.toList()}")

		/**
		 * ```javascript
		 * db.collection("drafts").aggregate([
		 *     …,
		 *     {
		 *         $unionWith: {
		 *             coll: 'invoiceA',
		 *             pipeline: [ … ]
		 *         }
		 *     },
		 *     {
		 *         $unionWith: {
		 *             coll: 'invoiceB',
		 *             pipeline: [ … ]
		 *         }
		 *     },
		 *     {
		 *         $unionWith: {
		 *             coll: 'invoiceC',
		 *             pipeline: [ … ]
		 *         }
		 *     },
		 *     commonSort,
		 *     commonLimit,
		 * ])
		 * ```
		 */
		val results = selectedDrafts
			.unionWith(selectedInvoicesA)
			.unionWith(selectedInvoicesB)
			.unionWith(selectedInvoicesC)
			.commonSort()
			.commonLimit()
			.also { println("Full pipeline: $it") }
			.toList()
		println("Results: $results")

		check(expected == results)
	}

}
