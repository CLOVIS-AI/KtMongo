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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.UpdateOptions
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.prepared.runner.kotest.PreparedSpec
import kotlin.time.Duration.Companion.minutes

@LowLevelApi
class WriteConcernTest : PreparedSpec({

	test("Set majority acknowledgment") {
		val options = UpdateOptions<Any>(testContext())

		options.writeConcern(WriteConcern(WriteAcknowledgment.Majority))

		options.toString() shouldBeBson """
			{
				"writeConcern": {
					"w": "majority"
				}
			}
		""".trimIndent()
	}

	test("Set acknowledgement of 5 nodes") {
		val options = UpdateOptions<Any>(testContext())

		options.writeConcern(WriteConcern(WriteAcknowledgment.Nodes(5)))

		options.toString() shouldBeBson """
			{
				"writeConcern": {
					"w": 5
				}
			}
		""".trimIndent()
	}

	test("Set acknowledgement with a tag") {
		val options = UpdateOptions<Any>(testContext())

		options.writeConcern(WriteConcern(WriteAcknowledgment.Tagged("backup")))

		options.toString() shouldBeBson """
			{
				"writeConcern": {
					"w": "backup"
				}
			}
		""".trimIndent()
	}

	test("Set acknowledgement to the journal") {
		val options = UpdateOptions<Any>(testContext())

		options.writeConcern(WriteConcern(writeToJournal = true))

		options.toString() shouldBeBson """
			{
				"writeConcern": {
					"j": true
				}
			}
		""".trimIndent()
	}

	test("Set an acknowledgment timeout") {
		val options = UpdateOptions<Any>(testContext())

		options.writeConcern(WriteConcern(writeTimeout = 2.minutes))

		options.toString() shouldBeBson """
			{
				"writeConcern": {
					"wtimeout": 120000
				}
			}
		""".trimIndent()
	}

	test("Everything together") {
		val options = UpdateOptions<Any>(testContext())

		options.writeConcern(
			WriteConcern(
				acknowledgment = WriteAcknowledgment.Majority,
				writeToJournal = false,
				writeTimeout = 2.minutes,
			)
		)

		options.toString() shouldBeBson """
			{
				"writeConcern": {
					"w": "majority",
					"j": false,
					"wtimeout": 120000
				}
			}
		""".trimIndent()
	}

	test("Shortcut: majority") {
		val options = UpdateOptions<Any>(testContext())

		options.writeConcern(WriteConcern.Majority)

		options.toString() shouldBeBson """
			{
				"writeConcern": {
					"w": "majority",
					"j": true
				}
			}
		""".trimIndent()
	}

	test("Shortcut: primary") {
		val options = UpdateOptions<Any>(testContext())

		options.writeConcern(WriteConcern.Primary)

		options.toString() shouldBeBson """
			{
				"writeConcern": {
					"w": 1,
					"j": false
				}
			}
		""".trimIndent()
	}

	test("Shortcut: primary") {
		val options = UpdateOptions<Any>(testContext())

		options.writeConcern(WriteConcern.FireAndForget)

		options.toString() shouldBeBson """
			{
				"writeConcern": {
					"w": 0,
					"j": false
				}
			}
		""".trimIndent()
	}

})
