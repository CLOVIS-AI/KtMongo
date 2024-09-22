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

package opensavvy.ktmongo.dsl

/**
 * Annotation to mark parts of the library that are known to be easy to misuse.
 *
 * These APIs may still need to be public to empower library authors to add missing operators.
 * However, they should be used very carefully, because they risk creating situations in which memory
 * leaks, injection attacks or other similar unwanted situations happen.
 *
 * If you truly think you need to use an API annotated by this annotation, please
 * contact us with your use-case so we can provide a safe alternative.
 */
@RequiresOptIn("This is a declaration from the low-level API which is used internally. We recommend against using it when possible, because it has less safety features and can easily create situations in which injections are possible. Behavior may also change between versions without warnings.", RequiresOptIn.Level.ERROR)
@MustBeDocumented
annotation class DangerousMongoApi

/**
 * Annotation that marks parts of the library that are not meant to be used by end-users.
 *
 * If you are developing a regular application interacting with MongoDB, you most likely do
 * not need to use functionality annotated with this annotation.
 *
 * Functionality provided behind this annotation is meant for library-authors creating new features
 * on top of this library, or for situations in which you want to implement a missing feature
 * (e.g. implement a missing operator).
 */
@RequiresOptIn("This is a declaration from the low-level API which is used internally. It is not recommended for regular users to interact with the low-level API.", RequiresOptIn.Level.WARNING)
@MustBeDocumented
annotation class LowLevelApi
