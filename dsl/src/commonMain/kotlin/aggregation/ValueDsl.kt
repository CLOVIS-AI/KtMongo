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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import kotlin.reflect.KProperty1

/**
 * DSL to instantiate [aggregation values][Value], usually automatically added into scope by aggregation stages.
 */
interface ValueDsl : FieldDsl {

	@LowLevelApi
	val context: BsonContext

	// TODO: document once we have a few more operators
	@OptIn(LowLevelApi::class)
	fun <Context : Any, Result> of(field: Field<Context, Result>): Value<Context, Result> =
		FieldValue(field, context)

	// TODO: document once we have a few more operators
	fun <Context : Any, Result> of(field: KProperty1<Context, Result>): Value<Context, Result> =
		of(field.field)

	// TODO: document once we have a few more operators
	@OptIn(LowLevelApi::class)
	fun <Context : Any, Result> of(value: Result): Value<Context, Result> =
		LiteralValue(value, context)

}

private class FieldValue<Context : Any, Result>(
	val field: Field<Context, Result>,
	context: BsonContext,
) : AbstractValue<Context, Result>(context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) {
		writer.writeString("$$field")
	}
}

private class LiteralValue<Context : Any, Result>(
	val value: Any?,
	context: BsonContext,
) : AbstractValue<Context, Result>(context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) {
		writer.writeDocument {
			writeObjectSafe("\$literal", value, context)
		}
	}
}
