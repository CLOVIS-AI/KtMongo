package fr.qsh.ktmongo.dsl

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
@RequiresOptIn("This is a declaration from the low-level API which is used internally. We recommend against using it when possible, because it has less safety features and is thus easy to misuse.")
@MustBeDocumented
annotation class LowLevelApi
