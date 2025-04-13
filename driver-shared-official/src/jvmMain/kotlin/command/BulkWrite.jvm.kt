/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.official.command

import com.mongodb.client.model.*
import opensavvy.ktmongo.bson.official.Bson
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.*

@Suppress("UNCHECKED_CAST")
@OptIn(LowLevelApi::class)
fun <Document> AvailableInBulkWrite<Document>.toJava(): WriteModel<Document> = when (this) {
	is UpdateMany<*> -> UpdateManyModel<Document>(
		/* filter = */ (context.buildDocument(this.filter) as Bson).raw,
		/* update = */ (context.buildDocument(this.update) as Bson).raw,
		/* options = */ com.mongodb.client.model.UpdateOptions(),
	)

	is UpdateOne<*> -> UpdateOneModel<Document>(
		/* filter = */ (context.buildDocument(this.filter) as Bson).raw,
		/* update = */ (context.buildDocument(this.update) as Bson).raw,
		/* options = */ com.mongodb.client.model.UpdateOptions(),
	)

	is UpsertOne<*> -> UpdateOneModel<Document>(
		/* filter = */ (context.buildDocument(this.filter) as Bson).raw,
		/* update = */ (context.buildDocument(this.update) as Bson).raw,
		/* options = */ com.mongodb.client.model.UpdateOptions().upsert(true),
	)

	is InsertOne<*> -> InsertOneModel<Document>(
		/* document = */ (this.document as Document)!!
	)

	is DeleteOne<*> -> DeleteOneModel<Document>(
		/* filter = */ (context.buildDocument(this.filter) as Bson).raw,
		/* options = */ DeleteOptions(),
	)

	is DeleteMany<*> -> DeleteManyModel<Document>(
		/* filter = */ (context.buildDocument(this.filter) as Bson).raw,
		/* options = */ DeleteOptions(),
	)
}
