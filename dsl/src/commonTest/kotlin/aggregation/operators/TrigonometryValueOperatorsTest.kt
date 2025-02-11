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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.dsl.aggregation.*
import opensavvy.prepared.runner.kotest.PreparedSpec

val acos = "\$acos"
val acosh = "\$acosh"
val asin = "\$asin"
val asinh = "\$asinh"
val atan = "\$atan"
val atanh = "\$atanh"

class TrigonometryValueOperatorsTest : PreparedSpec({
	class Target(
		val a: Double,
		val b: Double,
		val c: Double,
	)

	val a = "\$a"
	val b = "\$b"
	val c = "\$c"

	test(acos) {
		TestPipeline<Target>()
			.set {
				Target::c set acos(of(Target::a) + of(Target::b))
			}
			.shouldBeBson("""
					[
						{
							"$set": {
								"c": {
									"$acos": {
										"$add": [
											"$a",
											"$b"
										]
									}
								}
							}
						}
					]
				""".trimIndent())
	}

	test(acosh) {
		TestPipeline<Target>()
			.set {
				Target::c set acosh(of(2.0))
			}
			.shouldBeBson("""
					[
						{
							"$set": {
								"c": {
									"$acosh": {
										"$literal": 2.0
									}
								}
							}
						}
					]
				""".trimIndent())
	}

	test(asin) {
		TestPipeline<Target>()
			.set {
				Target::b set asin(of(Target::c))
			}
			.shouldBeBson("""
					[
						{
							"$set": {
								"b": {
									"$asin": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test(asinh) {
		TestPipeline<Target>()
			.set {
				Target::b set asinh(of(Target::c))
			}
			.shouldBeBson("""
					[
						{
							"$set": {
								"b": {
									"$asinh": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test(atan) {
		TestPipeline<Target>()
			.set {
				Target::b set atan(of(Target::c))
			}
			.shouldBeBson("""
					[
						{
							"$set": {
								"b": {
									"$atan": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test(atanh) {
		TestPipeline<Target>()
			.set {
				Target::b set atanh(of(Target::c))
			}
			.shouldBeBson("""
					[
						{
							"$set": {
								"b": {
									"$atanh": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

})
