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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.bson.multiplatform

import opensavvy.ktmongo.bson.validateBsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.prepared
import kotlin.time.measureTime

val context by prepared {
	BsonFactory()
}

val MultiplatformBsonWriterTest by preparedSuite {
	validateBsonFactory(context)

	test("Bson.eager()") {
		val document = context().buildDocument {
			writeString("foo", "bar")
			writeArray("baz") {
				writeString("a")
				writeString("b")
			}
			writeInt32("a", 12)
			writeArray("bad") {
				writeString("a")
				writeString("b")
			}
		}

		val lazyTime = measureTime {
			check(document.reader().read("baz")?.readArray()?.read(0)?.readString() == "a")
		}

		println("*** Initializing everything eagerly ***")
		document.eager()

		val eagerTime = measureTime {
			check(document.reader().read("bad")?.readArray()?.read(1)?.readString() == "b")
		}

		check(eagerTime <= lazyTime)

		document.eager() // Allowed, does nothing
	}

	test("BsonArray.eager()") {
		val document = context().buildArray {
			writeDocument {
				writeString("foo", "foo")
			}
			writeInt32(12)
			writeDocument {
				writeString("bar", "bar")
			}
		}

		val lazyTime = measureTime {
			check(document.reader().read(0)?.readDocument()?.read("foo")?.readString() == "foo")
		}

		println("*** Initializing everything eagerly ***")
		document.eager()

		val eagerTime = measureTime {
			check(document.reader().read(2)?.readDocument()?.read("bar")?.readString() == "bar")
		}

		check(eagerTime <= lazyTime)

		document.eager() // Allowed, does nothing
	}
}
