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

@file:JvmName("Query")

package opensavvy.ktmongo.sync

import opensavvy.ktmongo.dsl.options.Options
import opensavvy.ktmongo.dsl.options.SortOptionDsl
import opensavvy.ktmongo.dsl.options.WithSort
import opensavvy.ktmongo.dsl.query.FilterQuery
import java.util.function.Consumer

/**
 * Helper for Java users to specify the default options for a request.
 *
 * ### Example
 *
 * ```java
 * import static opensavvy.ktmongo.sync.Query.*;
 *
 * collection.find(options(), filter(filter -> {
 *     filter.eq(JavaField.of(User::name), "Bob");
 * })).toList();
 * ```
 */
fun <O : Options> options(): (O) -> Unit {
	return {}
}

/**
 * Helper for Java users to specify options for a given request.
 *
 * ### Example
 *
 * ```java
 * import static opensavvy.ktmongo.sync.Query.*;
 *
 * collection.find(
 *     options(options -> {
 *         options.limit(10);
 *         options.sort(sort(sort -> {
 *             sort.ascending(JavaField.of(User::age))
 *         }));
 *     }),
 *     filter(filter -> {
 *         filter.eq(JavaField.of(User::name), "Bob");
 *     })
 * ).toList();
 * ```
 */
fun <O : Options> options(block: Consumer<O>): (O) -> Unit {
	return { block.accept(it) }
}

/**
 * Helper for Java users of the find/filter methods.
 *
 * ### Example
 *
 * ```java
 * import static opensavvy.ktmongo.sync.Query.*;
 *
 * collection.find(options(), filter(filter -> {
 *     filter.eq(JavaField.of(User::name), "Bob");
 * })).toList();
 * ```
 *
 * ### Why?
 *
 * On the JVM, lambdas are not first-class citizens: they are emulated using SAM conversions.
 * This plays poorly with genericity, because Java's `void` type cannot be represented as part of
 * a type parameter (it isn't a subtype of `java.lang.Object`—in fact, it isn't a type at all).
 *
 * Kotlin doesn't have a concept of `void`, instead using a singleton called `Unit`. In Kotlin
 * code, this is invisible because `Unit` is implicit, but Java consumers need to explicitly specify
 * it:
 * ```java
 * collection.find(it -> Unit.INSTANCE, filter -> {
 *     filter.eq(JavaField.of(User::name), "foo");
 *     return Unit.INSTANCE;
 * })
 * ```
 * To facilitate using Kotlin lambdas from Java, the `Query` class contains helpers to declare
 * Java-style lambdas instead of using Kotlin-style lambdas directly, allowing us to write the
 * following code:
 * ```java
 * import static opensavvy.ktmongo.sync.Query.*;
 *
 * collection.find(options(), filter(filter -> {
 *     filter.eq(JavaField.of(User::name), "foo");
 * }));
 * ```
 */
fun <T> filter(block: Consumer<FilterQuery<T>>): (FilterQuery<T>) -> Unit {
	return { block.accept(it) }
}

/**
 * Helper for Java users to specify the sort order within the [WithSort.sort] option.
 *
 * ### Example
 *
 * ```java
 * import static opensavvy.ktmongo.sync.Query.*;
 *
 * collection.find(
 *     options(options -> {
 *         options.limit(10);
 *         options.sort(sort(sort -> {
 *             sort.ascending(JavaField.of(User::age))
 *         }));
 *     }),
 *     filter(filter -> {
 *         filter.eq(JavaField.of(User::name), "Bob");
 *     })
 * ).toList();
 * ```
 */
fun <T : Any> sort(block: Consumer<SortOptionDsl<T>>): (SortOptionDsl<T>) -> Unit {
	return { block.accept(it) }
}
