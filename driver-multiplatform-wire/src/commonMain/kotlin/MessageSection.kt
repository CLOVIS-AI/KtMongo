/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.multiplatform.wire

import opensavvy.ktmongo.bson.multiplatform.BsonDocument

sealed interface MessageSection {

	val kind: UByte

	class Body(
		val document: BsonDocument,
	) : MessageSection {
		override val kind: UByte
			get() = Body.kind

		override fun toString() = "Body($document)"

		companion object {
			const val kind: UByte = 0u
		}
	}

	class DocumentSequence(
		val id: String,
		val documents: List<BsonDocument>,
	) : MessageSection {
		override val kind: UByte
			get() = DocumentSequence.kind

		override fun toString() = "DocumentSequence('$id': $documents)"

		companion object {
			const val kind: UByte = 1u
		}
	}
}
