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

package opensavvy.ktmongo.dsl.path

import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KProperty1

/**
 * Allows configuring how the DSL generates property paths from
 */
interface PropertyNameStrategy {

	/**
	 * Generates the name of a [property].
	 *
	 * This is used by the DSL to allow configuring how the notation `Foo::bar / Bar::baz` is
	 * converted into a MongoDB path.
	 *
	 * For example, an implementation could add support for the KMongo annotation `@BsonId` to rename
	 * the field `_id`.
	 */
	@LowLevelApi
	fun nameOf(property: KProperty1<*, *>): String

	/**
	 * Default implementation of [PropertyNameStrategy], which always uses the property name.
	 */
	object Default : PropertyNameStrategy {

		@LowLevelApi
		override fun nameOf(property: KProperty1<*, *>): String =
			property.name
	}
}
