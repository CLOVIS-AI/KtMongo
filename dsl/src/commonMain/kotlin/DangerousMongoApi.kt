/*
 * Copyright (c) 2024, OpenSavvy, 4SH and contributors.
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

package opensavvy.ktmongo.dsl

/**
 * Annotation to mark parts of the library that are not meant to be used by end-users.
 *
 * They are still public because they may be needed by driver implementations,
 * or to in case some functionality is missing in the high-level API
 * (which is everything *not* annotated by this annotation).
 *
 * Users should be cautious about using functionality from the low-level API, as it may
 * not protect against incorrect usage. This could leak to injection attacks, memory leaks, or
 * other unwanted consequences.
 */
@RequiresOptIn("This is a declaration from the low-level API which is used internally. We recommend against using it when possible, because it has less safety features and can easily create situations in which injections are possible. Behavior may also change between versions without warnings.", RequiresOptIn.Level.ERROR)
@MustBeDocumented
annotation class DangerousMongoApi
