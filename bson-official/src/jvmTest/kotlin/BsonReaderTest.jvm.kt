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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.kotest.PreparedSpec

@OptIn(LowLevelApi::class)
class BsonReaderTest : PreparedSpec({

	test("Read basic data types") {
		val output = testContext().buildDocument {
			writeString("hello", "world")
			writeInt32("a", 42)
		}

		check(output.read().read("hello")?.readString() == "world")
		check(output.read().read("a")?.readInt32() == 42)
	}

})
