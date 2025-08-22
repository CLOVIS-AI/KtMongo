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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractValue
import opensavvy.ktmongo.dsl.aggregation.Value

/**
 * String aggregation operators.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/#string-expression-operators)
 */
@KtMongoDsl
interface StringValueOperators : ValueOperators {

	// region $trim

	/**
	 * Removes whitespace characters, including null, or the specified characters from the beginning and end of a string.
	 *
	 * By default, removes whitespace characters including the null character.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val text: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trim()
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/trim/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.trim(): Value<Context, String?> =
		TrimValueOperator(context, this, null)

	/**
	 * Removes the specified [characters] from the beginning and end of a string.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val text: String,
	 * )
	 *
	 * // Trim both 'g' and 'e' characters from the beginning and end
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trim('g', 'e')
	 *     }.toList()
	 *
	 * // Trim space, 'g', and 'e' characters from the beginning and end
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trim(' ', 'g', 'e')
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/trim/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.trim(vararg characters: Char): Value<Context, String?> =
		TrimValueOperator(context, this, of(characters.joinToString(separator = "")))

	/**
	 * Removes the specified [characters] from the beginning and end of a string.
	 *
	 * The [characters] parameter is a single string that can contain multiple characters to be trimmed.
	 * Each character in the string will be removed from both the beginning and end of the input string.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val text: String,
	 * )
	 *
	 * // Trim both 'g' and 'e' characters from the beginning and end
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trim(chars = of("ge"))
	 *     }.toList()
	 *
	 * // Trim space, 'g', and 'e' characters from the beginning and end
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trim(chars = of(" ge"))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/trim/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.trim(characters: Value<Context, String?>): Value<Context, String?> =
		TrimValueOperator(context, this, characters)

	@LowLevelApi
	private class TrimValueOperator<Context : Any>(
		context: BsonContext,
		private val input: Value<Context, String?>,
		private val chars: Value<Context, String?>?,
	) : AbstractValue<Context, String?>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeDocument("\$trim") {
					write("input") {
						input.writeTo(this)
					}
					if (chars != null) {
						write("chars") {
							chars.writeTo(this)
						}
					}
				}
			}
		}
	}

	// endregion
}
