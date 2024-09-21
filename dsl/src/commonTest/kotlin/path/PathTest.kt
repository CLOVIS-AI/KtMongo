/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.path

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.PathSegment.*
import opensavvy.prepared.runner.kotest.PreparedSpec

@OptIn(LowLevelApi::class)
class PathTest : PreparedSpec({

	test("Root field") {
		check(Path("test").toString() == "test")
	}

	test("Nested field") {
		check((Path("test") / Field("bar")).toString() == "test.bar")
	}

	test("Deeper nested field") {
		check((Path("test") / Field("bar") / Field("foo")).toString() == "test.bar.foo")
	}

	test("Indexed") {
		check((Path("test") / Indexed(3) / Field("bar")).toString() == "test.$3.bar")
	}

	test("Positional") {
		check((Path("test") / Positional / Field("bar")).toString() == "test.$.bar")
	}

	test("All positional") {
		check((Path("test") / AllPositional / Field("bar")).toString() == "test.$[].bar")
	}

})
