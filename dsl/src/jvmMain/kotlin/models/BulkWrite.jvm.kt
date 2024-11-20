/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.models

import com.mongodb.client.model.*
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.common.toBsonDocument

@Suppress("UNCHECKED_CAST")
@OptIn(LowLevelApi::class)
fun <Document> AvailableInBulkWrite<Document>.toJava(): WriteModel<Document> = when (this) {
	is UpdateMany<*> -> UpdateManyModel<Document>(
		/* filter = */ this.filter.toBsonDocument(),
		/* update = */ this.update.toBsonDocument(),
		/* options = */ UpdateOptions(),
	)

	is UpdateOne<*> -> UpdateOneModel<Document>(
		/* filter = */ this.filter.toBsonDocument(),
		/* update = */ this.update.toBsonDocument(),
		/* options = */ UpdateOptions(),
	)

	is UpsertOne<*> -> UpdateOneModel<Document>(
		/* filter = */ this.filter.toBsonDocument(),
		/* update = */ this.update.toBsonDocument(),
		/* options = */ UpdateOptions().upsert(true),
	)

	is InsertOne<*> -> InsertOneModel<Document>(
		/* document = */ (this.document as Document)!!
	)
}
