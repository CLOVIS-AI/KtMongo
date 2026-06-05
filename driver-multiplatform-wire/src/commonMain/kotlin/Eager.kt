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

package opensavvy.ktmongo.multiplatform.wire

/**
 * Instantiates a [Lazy] value that isn't lazy.
 *
 * This allows our API to contain lazy values without forcing us to be lazy everywhere.
 *
 * For example, we often want to be lazy during request sending (so all serialization happens as close as possible to the socket)
 * but not during reception (to extract information as quickly as possible and return the lock).
 */
internal fun <T> eager(value: T): Lazy<T> =
	object : Lazy<T> {
		override val value: T
			get() = value

		override fun isInitialized(): Boolean =
			true

		override fun toString(): String =
			"Lazy($value)"
	}
