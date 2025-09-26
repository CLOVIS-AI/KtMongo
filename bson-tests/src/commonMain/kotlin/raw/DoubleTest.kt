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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.bson.raw

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.serialize
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.verify
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import kotlin.Double.Companion.NaN

/**
 * Test boolean representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/double.json.
 */
fun SuiteDsl.double(context: Prepared<BsonContext>) = suite("Double") {
	@Serializable
	data class D(val d: Double)

	testBson(
		context,
		"+1.0",
		document { writeDouble("d", 1.0) },
		serialize(D(1.0)),
		hex("10000000016400000000000000F03F00"),
		json("""{"d": 1.0}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == 1.0)
		}
	)

	testBson(
		context,
		"-1.0",
		document { writeDouble("d", -1.0) },
		serialize(D(-1.0)),
		hex("10000000016400000000000000F0BF00"),
		json("""{"d": -1.0}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == -1.0)
		}
	)

	testBson(
		context,
		"+1.0001220703125",
		document { writeDouble("d", +1.0001220703125) },
		serialize(D(+1.0001220703125)),
		hex("10000000016400000000008000F03F00"),
		json("""{"d": 1.0001220703125}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == 1.0001220703125)
		}
	)

	testBson(
		context,
		"-1.0001220703125",
		document { writeDouble("d", -1.0001220703125) },
		serialize(D(-1.0001220703125)),
		hex("10000000016400000000008000F0BF00"),
		json("""{"d": -1.0001220703125}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == -1.0001220703125)
		}
	)

	testBson(
		context,
		"+1.2345678921232E+18",
		document { writeDouble("d", 1.2345678921232E+18) },
		serialize(D(1.2345678921232E+18)),
		hex("100000000164002A1BF5F41022B14300"),
		json("""{"d": 1.2345678921232E18}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == 1.2345678921232E+18)
		}
	)

	testBson(
		context,
		"-1.2345678921232E+18",
		document { writeDouble("d", -1.2345678921232E+18) },
		serialize(D(-1.2345678921232E+18)),
		hex("100000000164002A1BF5F41022B1C300"),
		json("""{"d": -1.2345678921232E18}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == -1.2345678921232E+18)
		}
	)

	testBson(
		context,
		"+0.0",
		document { writeDouble("d", 0.0) },
		serialize(D(0.0)),
		hex("10000000016400000000000000000000"),
		json("""{"d": 0.0}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == 0.0)
		}
	)

	testBson(
		context,
		"-0.0",
		document { writeDouble("d", -0.0) },
		serialize(D(-0.0)),
		hex("10000000016400000000000000008000"),
		json("""{"d": -0.0}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == -0.0)
		}
	)

	testBson(
		context,
		"NaN",
		document { writeDouble("d", NaN) },
		serialize(D(NaN)),
		hex("10000000016400000000000000F87F00"),
		json($$"""{"d": {"$numberDouble": "NaN"}}"""),
		verify("Read value") {
			check(read("d")?.readDouble()?.isNaN() == true)
		}
	)

	testBson(
		context,
		"+Infinity",
		document { writeDouble("d", Double.POSITIVE_INFINITY) },
		serialize(D(Double.POSITIVE_INFINITY)),
		hex("10000000016400000000000000F07F00"),
		json($$"""{"d": {"$numberDouble": "Infinity"}}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == Double.POSITIVE_INFINITY)
		}
	)

	testBson(
		context,
		"-Infinity",
		document { writeDouble("d", Double.NEGATIVE_INFINITY) },
		serialize(D(Double.NEGATIVE_INFINITY)),
		hex("10000000016400000000000000F0FF00"),
		json($$"""{"d": {"$numberDouble": "-Infinity"}}"""),
		verify("Read value") {
			check(read("d")?.readDouble() == Double.NEGATIVE_INFINITY)
		}
	)
}
