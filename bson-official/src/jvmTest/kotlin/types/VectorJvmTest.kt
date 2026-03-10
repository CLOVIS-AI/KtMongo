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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.bson.official.types

import opensavvy.ktmongo.bson.types.BooleanVector
import opensavvy.ktmongo.bson.types.ByteVector
import opensavvy.ktmongo.bson.types.FloatVector
import opensavvy.ktmongo.bson.types.Vector
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import kotlin.io.encoding.Base64

val OfficialVectorJvmSuite by preparedSuite {

	test("Vector binary-data round-trip") {
		val vector = Vector.fromBinaryData(Base64.encodeToByteArray("EAA=".toByteArray()))

		check(vector.toBinary().toKtMongoVector() == vector)
	}

	test("FloatVector round-trip") {
		val vector = FloatVector(127.0f, 7.2f, -19.5f, Float.NaN)

		check(vector.toOfficial().toKtMongo() == vector)
	}

	test("BooleanVector round-trip") {
		val vector = BooleanVector(true, true, false, false, false, true, false, false, false, true)

		check(vector.toOfficial().toKtMongo() == vector)
	}

	test("ByteVector round-trip") {
		val vector = ByteVector(1, -1, 127, 13, 0, 99)

		check(vector.toOfficial().toKtMongo() == vector)
	}

}
