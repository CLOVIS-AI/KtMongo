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

package opensavvy.ktmongo.dsl.path

import opensavvy.ktmongo.bson.BsonDocument
import opensavvy.ktmongo.bson.BsonPath
import opensavvy.ktmongo.bson.ExperimentalBsonPathApi
import opensavvy.ktmongo.bson.at
import opensavvy.ktmongo.bson.select
import opensavvy.ktmongo.bson.selectFirst
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.conversions.Bson

/**
 * Converts this MongoDB [Path] to a [BsonPath].
 *
 * [BsonPath] is an implementation of RFC9535 JSONPath.
 * See its documentation for more information.
 */
@LowLevelApi
@ExperimentalBsonPathApi
fun Path.toBsonPath(): BsonPath {
	var output: BsonPath = BsonPath

	this.asSequence().forEach {
		output = when (it) {
			is PathSegment.Field -> output[it.name]
			is PathSegment.Indexed -> output[it.index]
			PathSegment.Positional -> throw IllegalArgumentException($$"The positional operator (.$.) does not have an equivalent in the JSONPath RFC, so it cannot be converted to a BsonPath expression. Found: '$$this'")
			PathSegment.AllPositional -> output.all
		}
	}

	return output
}

/**
 * Converts this MongoDB [Field] to a [BsonPath].
 *
 * [BsonPath] is an implementation of RFC9535 JSONPath.
 * See its documentation for more information.
 *
 * [BsonPath] can be useful to access elements from an arbitrary BSON document,
 * for example, after a projection.
 *
 * ### Example
 *
 * ```kotlin
 * data class User(
 *     val _id: ObjectId,
 *     val profile: Profile,
 * )
 *
 * data class Profile(
 *     val name: String,
 * )
 *
 * val fieldName = (User::profile / Profile::name).toBsonPath()
 *
 * val bson: Bson = …
 * val name = bson at fieldName
 * ```
 *
 * @see select Select multiple values.
 * @see selectFirst Select the first value.
 * @see at Select the first value, as an infix operator.
 */
@OptIn(LowLevelApi::class)
@ExperimentalBsonPathApi
fun Field<*, *>.toBsonPath(): BsonPath =
	this.path.toBsonPath()

/**
 * Finds all values that match [field] in a given [BSON document][Bson].
 *
 * To learn more about the syntax, see [BsonPath].
 *
 * ### Example
 *
 * ```kotlin
 * val document: Bson = …
 *
 * document.select(User::profile / Profile::name)
 * ```
 * will return a sequence of all values matching the path `profile.name`.
 *
 * @see at Select a single value.
 */
@ExperimentalBsonPathApi
inline fun <reified T> BsonDocument.select(field: Field<*, T>): Sequence<T> =
	select(field.toBsonPath())

/**
 * Finds the first value that matches [field] in a given [BSON document][Bson].
 *
 * To learn more about the syntax, see [BsonPath].
 *
 * ### Example
 *
 * ```kotlin
 * val document: Bson = …
 *
 * document.selectFirst(User::profile / Profile::name)
 * ```
 * will return the value of the field `profile.name`.
 *
 * @see BsonPath Learn more about BSON paths.
 * @see select Select multiple values with a BSON path.
 * @see at Select a single value using infix notation.
 * @throws NoSuchElementException If no element is found matching the path.
 */
@ExperimentalBsonPathApi
inline fun <reified T> BsonDocument.selectFirst(field: Field<*, T>): T =
	selectFirst(field.toBsonPath())

/**
 * Finds the first value that matches [path] in a given [BSON document][Bson].
 *
 * ### Example
 *
 * ```kotlin
 * val document: Bson = …
 *
 * val bar: String = document at (User::profile / Profile::name)
 * ```
 * will return the value of the field `profile.name`.
 *
 * @see BsonPath Learn more about BSON paths.
 * @see select Select multiple values with a BSON path.
 * @throws NoSuchElementException If no element is found matching the path.
 */
@ExperimentalBsonPathApi
inline infix fun <reified T : Any?> BsonDocument.at(field: Field<*, T>): T =
	selectFirst(field)
