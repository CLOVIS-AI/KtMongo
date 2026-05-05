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

package opensavvy.ktmongo.bson.multiplatform.impl.read

internal fun String.encodeToJsonString(): String = buildString {
	for (c in this@encodeToJsonString) {
		when (c) {
			'"' -> append("\\\"")
			'\\' -> append("\\\\")
			'\b' -> append("\\b")
			'\t' -> append("\\t")
			'\n' -> append("\\n")
			'' -> append("\\f")
			'\r' -> append("\\r")
			else if (c.code < 0x20) -> append("\\u" + c.code.toString(16).padStart(4, '0'))
			else -> append(c)
		}
	}
}

internal fun String.encodeRegexToJsonString(): String = buildString {
	for (c in this@encodeRegexToJsonString) {
		when (c) {
			'\b' -> append("\\b")
			'\t' -> append("\\t")
			'\n' -> append("\\n")
			'' -> append("\\f")
			'\r' -> append("\\r")
			else if (c.code < 0x20) -> append("\\u" + c.code.toString(16).padStart(4, '0'))
			else -> append(c)
		}
	}
}
