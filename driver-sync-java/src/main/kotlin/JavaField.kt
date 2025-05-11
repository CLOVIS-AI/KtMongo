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

package opensavvy.ktmongo.sync

import com.github.meanbeanlib.mirror.Executables
import com.github.meanbeanlib.mirror.SerializableLambdas
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.path.PathSegment
import opensavvy.ktmongo.dsl.path.div
import org.bson.codecs.pojo.annotations.BsonId

/**
 * Type-safe representation of a dotted MongoDB field path from Java method references.
 *
 * This class is used to type-safely refer to fields from a DTO. For example, if we have the DTO:
 * ```java
 * public record Invoice(
 *     @BsonId ObjectId id,
 *     Customer customer
 * ) {
 *
 *     public record Customer(
 *         String name,
 *         String address
 *     ) {}
 * }
 * ```
 *
 * We can refer to the different fields as:
 * - `"_id"`: `JavaField.of(Invoice::id)`
 * - `"customer"`: `JavaField.of(Invoice::customer)`
 * - `"customer.name"`: `JavaField.of(Invoice::customer).child(Customer::name)`
 * - `"customer.address"`: `JavaField.of(Invoice::customer).child(Customer::address)`
 *
 * Because the generated reference is type-safe, you don't risk typos, and you can navigate from a DTO to
 * all its usages in all requests using your usual IDE tooling.
 *
 * **Important note.** If you're using KtMongo in a mixed Java-Kotlin project, note that this class has a slightly
 * different mapping algorithm than the Kotlin [Field] implementations, because Kotlin has field references
 * but Java doesn't, and Kotlin allows the name `_id` but Java doesn't.
 * In Kotlin code, KtMongo always uses the Kotlin field name as-is, whereas this class will use `_id` if the field
 * is annotated with `@BsonId`, remove the `"get"` prefix of a getter, etc. For the exact mapping algorithm, see
 * the implementation.
 *
 * @see JavaField.of Builds an instance of this class.
 */
@OptIn(LowLevelApi::class)
class JavaField<Root, Type> private constructor(
	override val path: Path,
) : Field<Root, Type> {

	/**
	 * Refers to a specific field that is a child of the current field.
	 *
	 * For more information, and for examples, see [JavaField].
	 */
	fun <T> child(accessor: SerializableLambdas.SerializableFunction1<Type, T>): JavaField<Root, T> {
		return JavaField(path / PathSegment.Field(accessor.name))
	}

	companion object {

		private val SerializableLambdas.SerializableFunction1<*, *>.name: String
			get() {
				val getter = Executables.findGetter(this)

				return when {
					getter.annotations.any { it is BsonId } -> "_id"
					getter.name.startsWith("get") -> getter.name.removePrefix("get").replaceFirstChar { it.lowercase() }
					getter.name.startsWith("is") -> getter.name.removePrefix("is").replaceFirstChar { it.lowercase() }
					else -> getter.name
				}
			}

		/**
		 * Type-safely refers to a MongoDB field based on its Java DTO.
		 *
		 * For more information, and for examples, see [JavaField].
		 */
		@JvmStatic
		fun <R, T> of(accessor: SerializableLambdas.SerializableFunction1<R, T>): JavaField<R, T> {
			return JavaField(Path(accessor.name))
		}
	}
}
