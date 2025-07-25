/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.query

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.tree.BsonNode
import org.intellij.lang.annotations.Language

expect fun testContext(): BsonContext

infix fun String.shouldBeBson(@Language("MongoDB-JSON") expected: String) {
	val expected = expected
		.replace("\n", "")
		.replace("\t", "")
		.replace(",", ", ")
		.replace("  ", " ")

	check(this == expected)
}

infix fun BsonNode.shouldBeBson(@Language("MongoDB-JSON") expected: String) {
	this.toString() shouldBeBson expected
}
