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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.BsonNode

/**
 * Specifies a validator for documents in a collection.
 *
 * To learn more about validation, see [HasValidation.validator].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/schema-validation/)
 */
class ValidatorOption(
	val validator: BsonNode,
	context: BsonContext,
) : AbstractOption("validator", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeDocument {
			validator.writeTo(this)
		}
	}
}

/**
 * Specifies the [validation level][ValidationLevel] in a collection.
 *
 * To learn more about validation, see [HasValidation.validator].
 */
class ValidationLevelOption(
	val level: ValidationLevel,
	context: BsonContext,
) : AbstractOption("validationLevel", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeString(level.bsonName)
	}
}

/**
 * Specifies the [validation action][ValidationAction] in a collection.
 *
 * To learn more about validation, see [HasValidation.validator].
 */
class ValidationActionOption(
	val action: ValidationAction,
	context: BsonContext,
) : AbstractOption("validationAction", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeString(action.bsonName)
	}
}

/**
 * Specifies how strictly MongoDB applies the validation rules.
 *
 * To learn more about validation, see [HasValidation.validator].
 */
enum class ValidationLevel(val bsonName: String) {
	/**
	 * Disables validation.
	 */
	Off("off"),

	/**
	 * Applies the validation rules on all writes.
	 */
	Strict("strict"),

	/**
	 * Applies the validation rules on modification to **existing valid documents**.
	 *
	 * Existing invalid documents are not validated.
	 */
	Moderate("moderate"),
	;
}

/**
 * Specifies how MongoDB reacts when a document doesn't pass validation.
 *
 * To learn more about validation, see [HasValidation.validator].
 */
enum class ValidationAction(val bsonName: String) {
	/**
	 * If validation fails, the write operation returns an error.
	 */
	Error("error"),

	/**
	 * If validation fails, the write operation continues and logs a warning.
	 */
	Warn("warn"),
	;
}

/**
 * Specifies validation rules for documents in a collection.
 *
 * To learn more about validation, see [HasValidation.validator].
 */
interface HasValidation<Document : Any> : Options {

	/**
	 * Specifies validation rules for a collection.
	 *
	 * By default, MongoDB accepts any document even if it doesn't look like other documents in the same collection.
	 *
	 * By declaring validation rules, MongoDB will reject inserts and updates that do not pass these rules, which
	 * can help ensure the collection isn't corrupted.
	 *
	 * ### Example
	 *
	 * For example, if we want to ensure all birth dates as stored as MongoDB's native [BsonType.Datetime] and never as
	 * a [BsonType.String].
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val birthdate: Instant,
	 * )
	 *
	 * users.create {
	 *     validator {
	 *         User::birthdate hasType BsonType.Datetime
	 *     }
	 * }
	 * ```
	 *
	 * If we try to insert a document with a string birthdate, for example in an external migration script, MongoDB will reject it.
	 *
	 * @see level How strict is MongoDB with the configured validator.
	 * @see action How MongoDB reacts when validation fails.
	 * @see HasBypassDocumentValidation.bypassDocumentValidation Allow a write operation to ignore the configured validator.
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun validator(
		level: ValidationLevel = ValidationLevel.Strict,
		action: ValidationAction = ValidationAction.Error,
		validator: FilterQuery<Document>.() -> Unit,
	) {
		accept(ValidatorOption(FilterQuery<Document>(context).apply(validator), context))
		accept(ValidationLevelOption(level, context))
		accept(ValidationActionOption(action, context))
	}

}
