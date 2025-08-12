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

@file:OptIn(LowLevelApi::class, DangerousMongoApi::class)

package opensavvy.ktmongo.bson.serialization

import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.EmptySerializersModule
import opensavvy.ktmongo.bson.BsonArray
import opensavvy.ktmongo.bson.multiplatform.Bson
import opensavvy.ktmongo.bson.multiplatform.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite

@Serializable
data class Sample(
	val x: Int,
	val y: String,
	val z: Sample? = null,
	val list: List<Int> = emptyList(),
	val map: Map<String, Int> = emptyMap(),
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
		if (map != other.map) return false
		if (!binaryData.contentEquals(other.binaryData)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = x
		result = 31 * result + y.hashCode()
		result = 31 * result + (z?.hashCode() ?: 0)
		result = 31 * result + list.hashCode()
		result = 31 * result + map.hashCode()
		result = 31 * result + binaryData.contentHashCode()
		return result
	}
}

val Testing by preparedSuite {

	val fullTest = Sample(
		x = 1,
		y = "hello",
		z = Sample(2, "world"),
		list = listOf(1, 2, 3),
		map = mapOf("one" to 1, "two" to 2),
		binaryData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
	)
	test("round trip class") {
		with(BsonContext()) {
			val content = fullTest.copy()
			val enc = BsonEncoderTopLevel(EmptySerializersModule(), this)
			Sample.serializer().serialize(enc, content)
			val bson = enc.out as Bson
			println("Complete bson: " + bson.toString())

			val dec = BsonDecoderTopLevel(EmptySerializersModule(), this, bson.toByteArray())
			dec.decodeSerializableValue(Sample.serializer()) shouldBe content
		}
	}
	test("round trip map") {
		with(BsonContext()) {
			val content = mapOf("one" to 1, "two" to 2)
			val ser = MapSerializer(String.serializer(), Int.serializer())
			val enc = BsonEncoderTopLevel(EmptySerializersModule(), this)
			ser.serialize(enc, content)
			val bson = enc.out as Bson
			println("Complete bson: " + bson.toString())

			val dec = BsonDecoderTopLevel(EmptySerializersModule(), this, bson.toByteArray())
			dec.decodeSerializableValue(ser) shouldBe content
		}
	}
	test("round trip list") {
		with(BsonContext()) {
			val content = listOf(fullTest, fullTest.copy(x = 2))
			val ser = ListSerializer(Sample.serializer())
			val enc = BsonEncoderTopLevel(EmptySerializersModule(), this)
			ser.serialize(enc, content)
			val bson = enc.out as BsonArray
			println("Complete bson: " + bson.toString())

			val dec = BsonDecoderTopLevel(EmptySerializersModule(), this, bson.toByteArray())
			dec.decodeSerializableValue(ser) shouldBe content
		}
	}
}