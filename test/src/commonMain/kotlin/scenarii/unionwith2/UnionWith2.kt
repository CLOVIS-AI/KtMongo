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

package opensavvy.ktmongo.tests.api.scenarii.documentscount

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.api.toList
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

// First, let's create the different data types & the collections

@Serializable
data class Customer(
	val _id: ObjectId,
)

@Serializable
data class Vendor(
	val _id: ObjectId,
)

@Serializable
data class AuditData(
	val creationDate: Int,
	val deletionDate: Int?,
)

@Serializable
data class TransactionalDocument(
	val _id: ObjectId,
	val vendor: Vendor,
	val customer: Customer,
	val auditData: AuditData? = null,
)

@Serializable
data class DocumentsNumber(
	val documentsNumber: Int,
)

@Serializable
data class DocumentsNumberTotal(
	val _id: String,
	val documentsNumber: Int,
)

/**
 * This is a case study of a complex aggregation pipeline using $match, $count, $unionWith and $group.
 *
 * The goal is to reproduce the following aggregation pipeline:
 * ```javascript
 * const commonMatch = {
 * 	'vendor._id': "507f1f77bcf86cd799439011", 'customer._id': "507f191e810c19729de860ea",
 * }
 *
 * db.getCollection("drafts").aggregate([
 * 	{
 * 		$match: {
 * 			'auditData.deletionDate': {$exists: false}, ...commonMatch
 * 		}
 * 	},
 * 	{
 * 		$count: 'documentsNumber'
 * 	},
 * 	{
 * 		$unionWith: {
 * 			coll: 'invoiceAggregates',
 * 			pipeline: [{$match: commonMatch}, {$count: 'documentsNumber'}]
 * 		}
 * 	},
 * 	{
 * 		$unionWith: {
 * 			coll: 'quoteAggregates',
 * 			pipeline: [{$match: commonMatch}, {$count: 'documentsNumber'}]
 * 		}
 * 	},
 * 	{
 * 		$unionWith: {
 * 			coll: 'creditNoteAggregates',
 * 			pipeline: [{$match: commonMatch}, {$count: 'documentsNumber'}]
 * 		}
 * 	},
 * 	{$group: {_id: 'groupAll', documentsNumber: {$sum: '$documentsNumber'}}}
 * ]);
 * ```
 */
fun SuiteDsl.verifyScenarioDocumentsCount(
	client: Prepared<MongoClient>,
) {

	val drafts by client.collection<TransactionalDocument>("scenario-documentscount-drafts")
	val invoiceAggregates by client.collection<TransactionalDocument>("scenario-documentscount-invoice-aggregates")
	val quoteAggregates by client.collection<TransactionalDocument>("scenario-documentscount-quote-aggregates")
	val creditNoteAggregates by client.collection<TransactionalDocument>("scenario-documentscount-credit-note-aggregates")

	test($$"Case study with $unionWith, $count and $group") {
		val vendorId = drafts().newId()
		val customerId = drafts().newId()
		val otherVendorId = drafts().newId()
		val otherCustomerId = drafts().newId()

		// 2 matching drafts, 1 deleted (excluded by the deletionDate check), 1 belonging to another customer
		drafts().insertOne(TransactionalDocument(drafts().newId(), Vendor(vendorId), Customer(customerId)))
		drafts().insertOne(TransactionalDocument(drafts().newId(), Vendor(vendorId), Customer(customerId)))
		drafts().insertOne(
			TransactionalDocument(
				drafts().newId(),
				Vendor(vendorId),
				Customer(customerId),
				AuditData(creationDate = 1, deletionDate = 2), // excluded: it was deleted
			)
		)
		drafts().insertOne(TransactionalDocument(drafts().newId(), Vendor(vendorId), Customer(otherCustomerId))) // excluded: wrong customer

		// 3 matching invoices, 1 belonging to another vendor
		invoiceAggregates().insertOne(TransactionalDocument(invoiceAggregates().newId(), Vendor(vendorId), Customer(customerId)))
		invoiceAggregates().insertOne(TransactionalDocument(invoiceAggregates().newId(), Vendor(vendorId), Customer(customerId)))
		invoiceAggregates().insertOne(TransactionalDocument(invoiceAggregates().newId(), Vendor(vendorId), Customer(customerId)))
		invoiceAggregates().insertOne(TransactionalDocument(invoiceAggregates().newId(), Vendor(otherVendorId), Customer(customerId))) // excluded: wrong vendor

		// 1 matching quote
		quoteAggregates().insertOne(TransactionalDocument(quoteAggregates().newId(), Vendor(vendorId), Customer(customerId)))

		// 2 matching credit notes, 1 belonging to another vendor and customer
		creditNoteAggregates().insertOne(TransactionalDocument(creditNoteAggregates().newId(), Vendor(vendorId), Customer(customerId)))
		creditNoteAggregates().insertOne(TransactionalDocument(creditNoteAggregates().newId(), Vendor(vendorId), Customer(customerId)))
		creditNoteAggregates().insertOne(TransactionalDocument(creditNoteAggregates().newId(), Vendor(otherVendorId), Customer(otherCustomerId))) // excluded

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
		fun FilterQuery<TransactionalDocument>.commonMatch() = and {
			TransactionalDocument::vendor / Vendor::_id eq vendorId
			TransactionalDocument::customer / Customer::_id eq customerId
		}

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
		 * 		$count: 'documentsNumber',
		 * 	},
		 * ])
		 * ```
		 */
		val draftsCount = drafts().aggregate()
			.match {
				(TransactionalDocument::auditData / AuditData::deletionDate).doesNotExist()
				commonMatch()
			}
			.countTo(DocumentsNumber::documentsNumber)

		/**
		 * ```javascript
		 * {
		 * 	coll: 'invoiceAggregates',
		 * 	pipeline: [{$match: commonMatch}, {$count: 'documentsNumber'}],
		 * }
		 * ```
		 */
		val invoiceAggregatesCount = invoiceAggregates().aggregate()
			.match { commonMatch() }
			.countTo(DocumentsNumber::documentsNumber)

		/**
		 * ```javascript
		 * {
		 * 	coll: 'quoteAggregates',
		 * 	pipeline: [{$match: commonMatch}, {$count: 'documentsNumber'}],
		 * }
		 * ```
		 */
		val quoteAggregatesCount = quoteAggregates().aggregate()
			.match { commonMatch() }
			.countTo(DocumentsNumber::documentsNumber)

		/**
		 * ```javascript
		 * {
		 * 	coll: 'creditNoteAggregates',
		 * 	pipeline: [{$match: commonMatch}, {$count: 'documentsNumber'}],
		 * }
		 * ```
		 */
		val creditNoteAggregatesCount = creditNoteAggregates().aggregate()
			.match { commonMatch() }
			.countTo(DocumentsNumber::documentsNumber)

		/**
		 * ```javascript
		 * db.collection("drafts").aggregate([
		 *     /* … */,
		 *     {$unionWith: {coll: 'invoiceAggregates', pipeline: […]}},
		 *     {$unionWith: {coll: 'quoteAggregates', pipeline: […]}},
		 *     {$unionWith: {coll: 'creditNoteAggregates', pipeline: […]}},
		 *     {$group: {_id: 'groupAll', documentsNumber: {$sum: '$documentsNumber'}}},
		 * ])
		 * ```
		 */
		val results = draftsCount
			.unionWith(invoiceAggregatesCount)
			.unionWith(quoteAggregatesCount)
			.unionWith(creditNoteAggregatesCount)
			.group {
				DocumentsNumberTotal::_id set "groupAll"
				DocumentsNumberTotal::documentsNumber sum DocumentsNumber::documentsNumber
			}
			.also { println("Full pipeline: $it") }
			.toList()
		println("Results: $results")

		check(results == listOf(DocumentsNumberTotal(_id = "groupAll", documentsNumber = 2 + 3 + 1 + 2)))
	}

}
