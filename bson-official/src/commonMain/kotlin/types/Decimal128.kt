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

package opensavvy.ktmongo.bson.official.types

/**
 * Binary integer decimal representation of a 128-bit decimal value, supporting 34 decimal digits
 * of significand and an exponent range of -6143 to +6144.
 */
expect class Decimal128 : Number, Comparable<Decimal128> {

	// This is not strictly not necessarily, but for some reason,
	// :bson:compileCommonMainKotlinMetadata fails without it
	override fun toByte(): Byte
	override fun toDouble(): Double
	override fun toFloat(): Float
	override fun toInt(): Int
	override fun toLong(): Long
	override fun toShort(): Short
	override fun compareTo(other: Decimal128): Int
}
