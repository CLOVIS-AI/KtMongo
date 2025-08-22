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
		TrimValueOperator(context, this, null, trimStart = true, trimEnd = true)

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
		TrimValueOperator(context, this, of(characters.joinToString(separator = "")), trimStart = true, trimEnd = true)

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
		TrimValueOperator(context, this, characters, trimStart = true, trimEnd = true)

	// endregion
	// region $ltrim

	/**
	 * Removes whitespace characters, including null, or the specified characters from the beginning of a string.
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
	 *         Document::text set of(Document::text).trimStart()
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/ltrim/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.trimStart(): Value<Context, String?> =
		TrimValueOperator(context, this, null, trimStart = true, trimEnd = false)

	/**
	 * Removes the specified [characters] from the beginning of a string.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val text: String,
	 * )
	 *
	 * // Trim both 'g' and 'e' characters from the beginning
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trimStart('g', 'e')
	 *     }.toList()
	 *
	 * // Trim space, 'g', and 'e' characters from the beginning
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trimStart(' ', 'g', 'e')
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/ltrim/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.trimStart(vararg characters: Char): Value<Context, String?> =
		TrimValueOperator(context, this, of(characters.joinToString(separator = "")), trimStart = true, trimEnd = false)

	/**
	 * Removes the specified [characters] from the beginning of a string.
	 *
	 * The [characters] parameter is a single string that can contain multiple characters to be trimmed.
	 * Each character in the string will be removed from the beginning of the input string.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val text: String,
	 * )
	 *
	 * // Trim both 'g' and 'e' characters from the beginning
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trimStart(characters = of("ge"))
	 *     }.toList()
	 *
	 * // Trim space, 'g', and 'e' characters from the beginning
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trimStart(characters = of(" ge"))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/ltrim/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.trimStart(characters: Value<Context, String?>): Value<Context, String?> =
		TrimValueOperator(context, this, characters, trimStart = true, trimEnd = false)

	// endregion
	// region $rtrim

	/**
	 * Removes whitespace characters, including null, or the specified characters from the end of a string.
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
	 *         Document::text set of(Document::text).trimEnd()
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/rtrim/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.trimEnd(): Value<Context, String?> =
		TrimValueOperator(context, this, null, trimStart = false, trimEnd = true)

	/**
	 * Removes the specified [characters] from the end of a string.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val text: String,
	 * )
	 *
	 * // Trim both 'g' and 'e' characters from the end
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trimEnd('g', 'e')
	 *     }.toList()
	 *
	 * // Trim space, 'g', and 'e' characters from the end
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trimEnd(' ', 'g', 'e')
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/rtrim/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.trimEnd(vararg characters: Char): Value<Context, String?> =
		TrimValueOperator(context, this, of(characters.joinToString(separator = "")), trimStart = false, trimEnd = true)

	/**
	 * Removes the specified [characters] from the end of a string.
	 *
	 * The [characters] parameter is a single string that can contain multiple characters to be trimmed.
	 * Each character in the string will be removed from the end of the input string.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val text: String,
	 * )
	 *
	 * // Trim both 'g' and 'e' characters from the end
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trimEnd(characters = of("ge"))
	 *     }.toList()
	 *
	 * // Trim space, 'g', and 'e' characters from the end
	 * collection.aggregate()
	 *     .set {
	 *         Document::text set of(Document::text).trimEnd(characters = of(" ge"))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/rtrim/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.trimEnd(characters: Value<Context, String?>): Value<Context, String?> =
		TrimValueOperator(context, this, characters, trimStart = false, trimEnd = true)

	// endregion
	// region $toLower

	/**
	 * Converts a string to lowercase, returning the result.
	 *
	 * If the argument resolves to `null`, `$toLower` returns an empty string `""`.
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
	 *         Document::text set of(Document::text).lowercase()
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/toLower/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.lowercase(): Value<Context, String?> =
		UnaryStringValueOperator(context, "toLower", this)

	// endregion

	@LowLevelApi
	private class UnaryStringValueOperator<Context : Any>(
		context: BsonContext,
		private val operator: String,
		private val value: Value<Context, String?>,
	) : AbstractValue<Context, String?>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("$$operator") {
					value.writeTo(this)
				}
			}
		}
	}

	@LowLevelApi
	private class TrimValueOperator<Context : Any>(
		context: BsonContext,
		private val input: Value<Context, String?>,
		private val chars: Value<Context, String?>?,
		private val trimStart: Boolean = true,
		private val trimEnd: Boolean = true,
	) : AbstractValue<Context, String?>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				val operator = when {
					trimStart && trimEnd -> "\$trim"
					trimStart -> "\$ltrim"
					trimEnd -> "\$rtrim"
					else -> throw IllegalArgumentException("At least one of trimStart or trimEnd must be true")
				}

				writeDocument(operator) {
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
