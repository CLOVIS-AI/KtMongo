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
import opensavvy.ktmongo.bson.write
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.prepared
import kotlin.io.encoding.Base64
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Not annotated with @Serializable: can only be serialized with :bson-kotlin
data class SerializableWithDataClass(
	val a: String,
	val b: org.bson.types.ObjectId,
	val c: opensavvy.ktmongo.bson.types.ObjectId,
	val d: Uuid,
)

// Not a data class: can only be serialized with :bson-kotlinx
@Serializable
class SerializableWithKxS(
	val a: String,
	val b: @Contextual org.bson.types.ObjectId,
	val c: opensavvy.ktmongo.bson.types.ObjectId,
	val d: Uuid,
)

val SerializationOptionsCompatibility by preparedSuite {

	val expectedBson by prepared {
		testContext().buildDocument {
			writeString("a", "Bob")
			writeObjectId("b", opensavvy.ktmongo.bson.types.ObjectId("640180000000000000000000"))
			writeObjectId("c", opensavvy.ktmongo.bson.types.ObjectId("640180000000000000000000"))
			writeBinaryData("d", 0x4u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		}
	}

	val valueDataClass by prepared {
		SerializableWithDataClass(
			a = "Bob",
			b = org.bson.types.ObjectId("640180000000000000000000"),
			c = opensavvy.ktmongo.bson.types.ObjectId("640180000000000000000000"),
			d = Uuid.parse("73ffd264-44b3-4c69-90e8-e7d1dfc035d4"),
		)
	}

	val valueKxS by prepared {
		SerializableWithKxS(
			a = "Bob",
			b = org.bson.types.ObjectId("640180000000000000000000"),
			c = opensavvy.ktmongo.bson.types.ObjectId("640180000000000000000000"),
			d = Uuid.parse("73ffd264-44b3-4c69-90e8-e7d1dfc035d4"),
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
