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

package opensavvy.ktmongo.multiplatform

import opensavvy.ktmongo.multiplatform.utils.MongoClient
import opensavvy.prepared.runner.testballoon.preparedSuite

val MultiplatformConnection by preparedSuite {

	test("Connect") {
		val client = MongoClient()

		println(client.context)
	}

}
