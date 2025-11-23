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

package opensavvy.ktmongo.utils.kmongo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.PropertyNameStrategy
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.prepared
import org.bson.codecs.pojo.annotations.BsonId
import kotlin.reflect.jvm.javaField

@Serializable
data class NameStrategyProfile(val name: String)

val KMongoNameStrategyTest by preparedSuite {

	val fieldDsl by prepared {
		object : FieldDsl {
			@LowLevelApi
			override val context: PropertyNameStrategy
				get() = KMongoNameStrategy()
		}
	}

	test("BsonId") {
		@Serializable
		data class Test1(
			@field:BsonId
			val a: NameStrategyProfile,
		)

		check(BsonId() in Test1::a.javaField?.annotations.orEmpty())

		with(fieldDsl()) {
			check((Test1::a / NameStrategyProfile::name).toString() == "_id.name")
		}
	}

	test("SerialName") {
		@Serializable
		data class Test2(
			@property:SerialName("foo")
			val a: NameStrategyProfile,
		)

		check(SerialName("foo") in Test2::a.annotations)

		with(fieldDsl()) {
			check((Test2::a / NameStrategyProfile::name).toString() == "foo.name")
		}
	}

}
