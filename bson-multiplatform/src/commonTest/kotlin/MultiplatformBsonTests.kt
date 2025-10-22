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

package opensavvy.ktmongo.bson.multiplatform

import opensavvy.ktmongo.bson.path.bsonPathTests
import opensavvy.ktmongo.bson.raw.*
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.prepared

val context by prepared {
	BsonFactory()
}

val MultiplatformBsonWriterTest by preparedSuite {
	boolean(context)
	int32(context)
	int64(context)
	double(context)
	string(context)
	reprNull(context)
	reprUndefined(context)
	document(context)
	array(context)
	binary(context)
	code(context)
	datetime(context)
	minMaxKey(context)
	regex(context)
	timestamp(context)
	objectId(context)

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	test("Pipe objects") {
		val pipe = context().buildDocument {
			writeInt32("four", 2 + 2)
			writeString("foo", "bar")
			writeArray("grades") {
				writeInt32(4)
				writeInt32(7)
			}
		}

		context().buildDocument {
			write("root") {
				pipe(pipe.reader().asValue())
			}
		} shouldBeJson """{"root": {"four": 4, "foo": "bar", "grades": [4, 7]}}"""
	}

	bsonPathTests(context)
}
