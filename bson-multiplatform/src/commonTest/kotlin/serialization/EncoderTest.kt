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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.bson.multiplatform.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.multiplatform.context
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite

@OptIn(ExperimentalSerializationApi::class)
val EncoderTest by preparedSuite {

	test("A simple class") {
		@Serializable
		data class User(val id: Int)

		check(encodeToBson(context(), User(1234)).toString() == """{"id": 1234}""")
	}

	suite("Complex example") {
		@Serializable
		data class Sample(
			val x: Int,
			val y: String,
			val z: Sample? = null,
			val list: List<Int> = emptyList(),
			val binaryData: ByteArray = byteArrayOf(),
		) {
			override fun equals(other: Any?): Boolean {
				if (this === other) return true
				if (other == null || this::class != other::class) return false

				other as Sample

				if (x != other.x) return false
				if (y != other.y) return false
				if (z != other.z) return false
				if (list != other.list) return false
				if (!binaryData.contentEquals(other.binaryData)) return false

				return true
			}

			override fun hashCode(): Int {
				var result = x
				result = 31 * result + y.hashCode()
				result = 31 * result + (z?.hashCode() ?: 0)
				result = 31 * result + list.hashCode()
				result = 31 * result + binaryData.contentHashCode()
				return result
			}
		}

		val fullTest = Sample(
			x = 1,
			y = "hello",
			z = Sample(2, "world"),
			list = listOf(1, 2, 3),
			binaryData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
		)

		test("Round trip class") {
			serializeRoundTrip(fullTest)
		}
	}
}
