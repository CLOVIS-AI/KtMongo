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

package opensavvy.ktmongo.bson

/**
 * Binary integer decimal representation of a 128-bit decimal value, supporting 34 decimal digits
 * of significand and an exponent range of -6143 to +6144.
 */
actual class Decimal128 : Number(), Comparable<Decimal128> {
	override fun toByte(): Byte {
		TODO("Not yet implemented")
	}

	override fun toDouble(): Double {
		TODO("Not yet implemented")
	}

	override fun toFloat(): Float {
		TODO("Not yet implemented")
	}

	override fun toInt(): Int {
		TODO("Not yet implemented")
	}

	override fun toLong(): Long {
		TODO("Not yet implemented")
	}

	override fun toShort(): Short {
		TODO("Not yet implemented")
	}

	override fun compareTo(other: Decimal128): Int {
		TODO("Not yet implemented")
	}
}
