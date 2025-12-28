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

import com.mongodb.MongoClientSettings
import opensavvy.ktmongo.bson.validateBsonFactory
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.prepared

val testContext by prepared {
	JvmBsonFactory(MongoClientSettings.getDefaultCodecRegistry())
}

@OptIn(ExperimentalStdlibApi::class)
val JvmBsonContextTest by preparedSuite {
	validateBsonFactory(testContext)
}
