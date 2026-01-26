/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class, LowLevelApi::class, ExperimentalBsonDiffApi::class)

package opensavvy.ktmongo.bson.official

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.ExperimentalBsonDiffApi
import opensavvy.ktmongo.bson.diff
import opensavvy.ktmongo.bson.read
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.bson.write
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.prepared
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

// Not annotated with @Serializable: can only be serialized with :bson-kotlin
data class SerializableWithDataClass(
	val a: String,
	val b: org.bson.types.ObjectId,
	val c: opensavvy.ktmongo.bson.types.ObjectId,
	val d: Timestamp,
)

// Not a data class: can only be serialized with :bson-kotlinx
@Serializable
class SerializableWithKxS(
	val a: String,
	val b: @Contextual org.bson.types.ObjectId,
	val c: opensavvy.ktmongo.bson.types.ObjectId,
	val d: Timestamp,
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is SerializableWithKxS) return false

		if (a != other.a) return false
		if (b != other.b) return false
		if (c != other.c) return false
		if (d != other.d) return false

		return true
	}

	override fun hashCode(): Int {
		var result = a.hashCode()
		result = 31 * result + b.hashCode()
		result = 31 * result + c.hashCode()
		result = 31 * result + d.hashCode()
		return result
	}

	override fun toString(): String {
		return "SerializableWithKxS(a='$a', b=$b, c=$c, d=$d)"
	}

}

val SerializationOptionsCompatibility by preparedSuite {

	val expectedBson by prepared {
		testContext().buildDocument {
			writeString("a", "Bob")
			writeObjectId("b", opensavvy.ktmongo.bson.types.ObjectId("640180000000000000000000"))
			writeObjectId("c", opensavvy.ktmongo.bson.types.ObjectId("640180000000000000000000"))
			writeTimestamp("d", Timestamp(Instant.parse("2023-03-01T00:00:00Z"), 12u))
		}
	}

	val valueDataClass by prepared {
		SerializableWithDataClass(
			a = "Bob",
			b = org.bson.types.ObjectId("640180000000000000000000"),
			c = opensavvy.ktmongo.bson.types.ObjectId("640180000000000000000000"),
			d = Timestamp(Instant.parse("2023-03-01T00:00:00Z"), 12u),
		)
	}

	val valueKxS by prepared {
		SerializableWithKxS(
			a = "Bob",
			b = org.bson.types.ObjectId("640180000000000000000000"),
			c = opensavvy.ktmongo.bson.types.ObjectId("640180000000000000000000"),
			d = Timestamp(Instant.parse("2023-03-01T00:00:00Z"), 12u),
		)
	}

	val valueDataClassBson by prepared {
		testContext().write(valueDataClass())
	}

	val valueKxSBson by prepared {
		testContext().write(valueKxS())
	}

	test("Compare :bson-kotlin to baseline") {
		check(valueDataClassBson() == expectedBson()) { "Diff:\n${valueDataClassBson() diff expectedBson()}" }
	}

	test("Compare :bson-kotlinx to baseline") {
		check(valueKxSBson() == expectedBson()) { "Diff:\n${valueKxSBson() diff expectedBson()}" }
	}

	test("Compare :bson-kotlin to :bson-kotlinx") {
		check(valueDataClassBson() == valueKxSBson()) { "Diff:\n${valueDataClassBson() diff valueKxSBson()}" }
	}

	test("Round-trip from :bson-kotlin to :bson-kotlinx") {
		check(valueDataClassBson().read<SerializableWithKxS>() == valueKxS())
	}

	test("Round-trip from :bson-kotlinx to :bson-kotlin") {
		check(valueKxSBson().read<SerializableWithDataClass>() == valueDataClass())
	}

}
