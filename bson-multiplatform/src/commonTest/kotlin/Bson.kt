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

import opensavvy.ktmongo.bson.raw.boolean
import opensavvy.ktmongo.bson.raw.int32
import opensavvy.ktmongo.bson.raw.int64
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.prepared

class MultiplatformBsonWriterTest : PreparedSpec({
	val context by prepared {
		BsonContext()
	}

	boolean(context)
	int32(context)
	int64(context)
})
