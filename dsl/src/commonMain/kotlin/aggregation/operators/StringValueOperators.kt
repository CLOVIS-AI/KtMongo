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
	// region $toUpper

	/**
	 * Converts a string to uppercase, returning the result.
	 *
	 * If the argument resolves to `null`, `$toUpper` returns an empty string `""`.
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
	 *         Document::text set of(Document::text).uppercase()
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/toUpper/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.uppercase(): Value<Context, String?> =
		UnaryStringValueOperator(context, "toUpper", this)

	// endregion
	// region $strLenCP

	/**
	 * Returns the number of code points in the specified string.
	 *
	 * If the argument resolves to `null`, this function returns `null`.
	 *
	 * ### Counting characters
	 *
	 * This function uses MongoDB's `$strLenCP` operator, which counts characters using Unicode code points.
	 * This differs from Kotlin's [String.length], which uses UTF-16 code units.
	 * For strings containing characters outside the Basic Multilingual Plane (like emoji or certain mathematical symbols),
	 * the counting behavior will differ.
	 *
	 * For example, the emoji "👨‍👩‍👧‍👦" (family) is a single Unicode grapheme cluster but consists of multiple code points.
	 * According to this operator, it has a length of 7.
	 * However, according to Kotlin's [String.length], it has a length of 11.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val text: String,
	 *     val length: Int,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::length set of(Document::text).length
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/strLenCP/)
	 *
	 * @see lengthUTF8
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	val <Context : Any> Value<Context, String?>.length: Value<Context, Int?>
		get() = StrLenCPValueOperator(context, this)

	// endregion
	// region $strLenBytes

	/**
	 * Returns the number of UTF-8 encoded bytes in the specified string.
	 *
	 * If the argument resolves to `null`, this function returns `null`.
	 *
	 * ### Counting characters
	 *
	 * This function uses MongoDB's `$strLenBytes` operator, which counts characters using UTF-8 encoded bytes where
	 * each code point, or character, may use between one and four bytes to encode.
	 * This differs from the [length] property which uses Unicode code points.
	 *
	 * For example, US-ASCII characters are encoded using one byte.
	 * Characters with diacritic markings and additional Latin alphabetical characters are encoded using two bytes.
	 * Chinese, Japanese and Korean characters typically require three bytes, and other planes of Unicode
	 * (emoji, mathematical symbols, etc.) require four bytes.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val text: String,
	 *     val byteLength: Int,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::byteLength set of(Document::text).lengthUTF8
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/strLenBytes/)
	 *
	 * @see length
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	val <Context : Any> Value<Context, String?>.lengthUTF8: Value<Context, Int?>
		get() = StrLenBytesValueOperator(context, this)

	// endregion
	// region $substrCP

	/**
	 * Returns the substring of a string.
	 *
	 * The substring starts with the character at the specified Unicode code point [startIndex] (zero-based) in the string and continues for the [length] number of code points specified.
	 *
	 * Note that this behavior is different from [String.substring], which expects start and end indexes.
	 *
	 * ### Counting characters
	 *
	 * This function uses MongoDB's `$substrCP` operator, which counts characters using Unicode code points.
	 * This differs from Kotlin's [String.substring], which uses UTF-16 code units.
	 * For strings containing characters outside the Basic Multilingual Plane (like emoji or certain mathematical symbols),
	 * the indexing behavior will differ.
	 *
	 * For example, the emoji "👨‍👩‍👧‍👦" (family) is a single Unicode grapheme cluster but consists of multiple code points.
	 * According to this operator, it has a size of 5.
	 * However, according to Kotlin's [String.substring], it has a size of 11.
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
	 *         Document::text set of(Document::text).substring(startIndex = of(1), length = of(2))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/substrCP/)
	 *
	 * @see substringUTF8
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.substring(startIndex: Value<Context, Int>, length: Value<Context, Int>): Value<Context, String?> =
		SubstrCPValueOperator(context, this, startIndex, length)

	/**
	 * Returns the substring of a string.
	 *
	 * The substring contains the Unicode code points that are contained within [indexes].
	 *
	 * ### Counting characters
	 *
	 * This function uses MongoDB's `$substrCP` operator, which counts characters using Unicode code points.
	 * This differs from Kotlin's [String.substring], which uses UTF-16 code units.
	 * For strings containing characters outside the Basic Multilingual Plane (like emoji or certain mathematical symbols),
	 * the indexing behavior will differ.
	 *
	 * For example, the emoji "👨‍👩‍👧‍👦" (family) is a single Unicode grapheme cluster but consists of multiple code points.
	 * According to this operator, it has a size of 5.
	 * However, according to Kotlin's [String.substring], it has a size of 11.
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
	 *         Document::text set of(Document::text).substring(1..2)
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/substrCP/)
	 *
	 * @see substringUTF8
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.substring(indexes: IntRange): Value<Context, String?> =
		SubstrCPValueOperator(context, this, of(indexes.first), of(indexes.last - indexes.first))

	// endregion
	// region $substrBytes

	/**
	 * Returns the substring of a string.
	 *
	 * The substring starts with the character at the specified UTF-8 byte [startIndex] in the string and continues for the [byteCount] number of bytes.
	 *
	 * Note that this behavior is different from [String.substring], which expects start and end indexes.
	 *
	 * ### Counting characters
	 *
	 * This function uses MongoDB's `$substrBytes` operator, which counts characters using UTF-8 encoded bytes where
	 * each code point, or character, may use between one and four bytes to encode.
	 * This differs from the [substring] function which uses Unicode code points.
	 *
	 * For example, US-ASCII characters are encoded using one byte.
	 * Characters with diacritic markings and additional Latin alphabetical characters are encoded using two bytes.
	 * Chinese, Japanese and Korean characters typically require three bytes, and other planes of Unicode
	 * (emoji, mathematical symbols, etc.) require four bytes.
	 *
	 * If [startIndex] or [byteCount] happen to be within a multibyte character, an error will be thrown.
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
	 *         Document::text set of(Document::text).substringUTF8(startIndex = of(1), byteCount = of(2))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/substrBytes/)
	 *
	 * @see substring
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.substringUTF8(startIndex: Value<Context, Int>, byteCount: Value<Context, Int>): Value<Context, String?> =
		SubstrBytesValueOperator(context, this, startIndex, byteCount)

	/**
	 * Returns the substring of a string.
	 *
	 * The substring contains the Unicode code points that are contained within [indexes].
	 *
	 * ### Counting characters
	 *
	 * This function uses MongoDB's `$substrBytes` operator, which counts characters using UTF-8 encoded bytes where
	 * each code point, or character, may use between one and four bytes to encode.
	 * This differs from the [substring] function which uses Unicode code points.
	 *
	 * For example, US-ASCII characters are encoded using one byte.
	 * Characters with diacritic markings and additional Latin alphabetical characters are encoded using two bytes.
	 * Chinese, Japanese and Korean characters typically require three bytes, and other planes of Unicode
	 * (emoji, mathematical symbols, etc.) require four bytes.
	 *
	 * If the start or end index happens to be within a multibyte character, an error will be thrown.
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
	 *         Document::text set of(Document::text).substringUTF8(1..2)
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/substrBytes/)
	 *
	 * @see substring
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.substringUTF8(indexes: IntRange): Value<Context, String?> =
		SubstrBytesValueOperator(context, this, of(indexes.first), of(indexes.last - indexes.first))

	// endregion
	// region $split

	/**
	 * Divides a string into an array of substrings based on a [delimiter].
	 *
	 * `$split` removes the delimiter and returns the resulting substrings as elements of an array.
	 * If the delimiter is not found in the string, `$split` returns the original string as the only element of an array.
	 *
	 * Both the string expression and delimiter must be strings. Otherwise, the operation fails with an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val city: String,
	 *     val cityState: List<String>,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::cityState set of(Document::city).split(of(", "))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/split/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String>.split(delimiter: Value<Context, String>): Value<Context, List<String>?> =
		SplitValueOperator(context, this, delimiter)

	/**
	 * Divides a string into an array of substrings based on a [delimiter].
	 *
	 * `$split` removes the delimiter and returns the resulting substrings as elements of an array.
	 * If the delimiter is not found in the string, `$split` returns the original string as the only element of an array.
	 *
	 * The string expression must be a string. Otherwise, the operation fails with an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val city: String,
	 *     val cityState: List<String>,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::cityState set of(Document::city).split(", ")
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/split/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String>.split(delimiter: String): Value<Context, List<String>?> =
		SplitValueOperator(context, this, of(delimiter))

	// endregion
	// region $replaceOne

	/**
	 * Replaces the first instance of [find] with a [replacement] string.
	 *
	 * If no occurrences of [find] are found in the input string, the input string is returned.
	 * If any of the input, [find] or [replacement] is `null`, `null` is returned.
	 *
	 * The input, [find], and [replacement] expressions must evaluate to a string or a `null`, or `$replaceOne` fails with an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val item: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::item set of(Document::item).replaceFirst(find = of("blue paint"), replacement = of("red paint"))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/replaceOne/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.replaceFirst(find: Value<Context, String?>, replacement: Value<Context, String?>): Value<Context, String?> =
		ReplaceValueOperator(context, "\$replaceOne", this, find, replacement)

	/**
	 * Replaces the first instance of [find] with a [replacement] string.
	 *
	 * If no occurrences of [find] are found in the input string, the input string is returned.
	 * If the input is `null`, `null` is returned.
	 *
	 * The input must evaluate to a string or a `null`, or `$replaceOne` fails with an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val item: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::item set of(Document::item).replaceFirst(find = "blue paint", replacement = "red paint")
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/replaceOne/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.replaceFirst(find: String, replacement: String): Value<Context, String?> =
		ReplaceValueOperator(context, "\$replaceOne", this, of(find), of(replacement))

	// endregion
	// region $replaceAll

	/**
	 * Replaces all instances of [find] with a [replacement] string.
	 *
	 * If no occurrences of [find] are found in the input string, the input string is returned.
	 * If any of the input, [find] or [replacement] is `null`, `null` is returned.
	 *
	 * The input, [find], and [replacement] expressions must evaluate to a string or a `null`, or `$replaceAll` fails with an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val item: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::item set of(Document::item).replace(find = of("blue paint"), replacement = of("red paint"))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/replaceAll/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.replace(find: Value<Context, String?>, replacement: Value<Context, String?>): Value<Context, String?> =
		ReplaceValueOperator(context, "\$replaceAll", this, find, replacement)

	/**
	 * Replaces all instances of [find] with a [replacement] string.
	 *
	 * If no occurrences of [find] are found in the input string, the input string is returned.
	 * If the input is `null`, `null` is returned.
	 *
	 * The input must evaluate to a string or a `null`, or `$replaceAll` fails with an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val item: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::item set of(Document::item).replace(find = "blue paint", replacement = "red paint")
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/replaceAll/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> Value<Context, String?>.replace(find: String, replacement: String): Value<Context, String?> =
		ReplaceValueOperator(context, "\$replaceAll", this, of(find), of(replacement))

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

	@LowLevelApi
	private class SubstrCPValueOperator<Context : Any>(
		context: BsonContext,
		private val input: Value<Context, String?>,
		private val startIndex: Value<Context, Int>,
		private val length: Value<Context, Int>,
	) : AbstractValue<Context, String?>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$substrCP") {
					input.writeTo(this)
					startIndex.writeTo(this)
					length.writeTo(this)
				}
			}
		}
	}

	@LowLevelApi
	private class SubstrBytesValueOperator<Context : Any>(
		context: BsonContext,
		private val input: Value<Context, String?>,
		private val startIndex: Value<Context, Int>,
		private val byteCount: Value<Context, Int>,
	) : AbstractValue<Context, String?>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$substrBytes") {
					input.writeTo(this)
					startIndex.writeTo(this)
					byteCount.writeTo(this)
				}
			}
		}
	}

	@LowLevelApi
	private class StrLenCPValueOperator<Context : Any>(
		context: BsonContext,
		private val input: Value<Context, String?>,
	) : AbstractValue<Context, Int?>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("\$strLenCP") {
					input.writeTo(this)
				}
			}
		}
	}

	@LowLevelApi
	private class StrLenBytesValueOperator<Context : Any>(
		context: BsonContext,
		private val input: Value<Context, String?>,
	) : AbstractValue<Context, Int?>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("\$strLenBytes") {
					input.writeTo(this)
				}
			}
		}
	}

	@LowLevelApi
	private class SplitValueOperator<Context : Any>(
		context: BsonContext,
		private val input: Value<Context, String?>,
		private val delimiter: Value<Context, String?>,
	) : AbstractValue<Context, List<String>?>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$split") {
					input.writeTo(this)
					delimiter.writeTo(this)
				}
			}
		}
	}

	@LowLevelApi
	private class ReplaceValueOperator<Context : Any>(
		context: BsonContext,
		private val operator: String,
		private val input: Value<Context, String?>,
		private val find: Value<Context, String?>,
		private val replacement: Value<Context, String?>,
	) : AbstractValue<Context, String?>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeDocument(operator) {
					write("input") {
						input.writeTo(this)
					}
					write("find") {
						find.writeTo(this)
					}
					write("replacement") {
						replacement.writeTo(this)
					}
				}
			}
		}
	}

	// region $concat

	/**
	 * Concatenates strings together.
	 *
	 * If any of strings are `null`, the concatenation returns `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val firstName: String,
	 *     val lastName: String,
	 *     val fullName: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::fullName set concat(of(Document::firstName), of(" "), of(Document::lastName))
	 *     }
	 *     .toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/concat/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> concat(strings: List<Value<Context, String?>>): Value<Context, String?> =
		ConcatValueOperator(context, strings)

	/**
	 * Concatenates strings together.
	 *
	 * If any of strings are `null`, the concatenation returns `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val firstName: String,
	 *     val lastName: String,
	 *     val fullName: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::fullName set concat(of(Document::firstName), of(" "), of(Document::lastName))
	 *     }
	 *     .toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/concat/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> concat(vararg strings: Value<Context, String?>): Value<Context, String?> =
		concat(strings.asList())

	/**
	 * Concatenates strings together.
	 *
	 * If any of strings are `null`, the concatenation returns `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Document(
	 *     val firstName: String,
	 *     val lastName: String,
	 *     val fullName: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Document::fullName set (of(Document::firstName) concat of(" ") concat of(Document::lastName))
	 *     }
	 *     .toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/concat/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <Context : Any> Value<Context, String?>.concat(other: Value<Context, String?>): Value<Context, String?> =
		concat(listOf(this, other))

	@LowLevelApi
	private class ConcatValueOperator<Context : Any>(
		context: BsonContext,
		private val strings: List<Value<Context, String?>>,
	) : AbstractValue<Context, String?>(context) {

		override fun simplify(): AbstractValue<Context, String?> {
			val flattenedOperands = ArrayList<Value<Context, String?>>()

			for (operand in strings) {
				if (operand is ConcatValueOperator) {
					flattenedOperands += operand.strings
				} else {
					flattenedOperands += operand
				}
			}

			return if (flattenedOperands != strings) {
				ConcatValueOperator(context, flattenedOperands)
			} else {
				this
			}
		}

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$concat") {
					for (str in strings) {
						str.writeTo(this)
					}
				}
			}
		}
	}
}
