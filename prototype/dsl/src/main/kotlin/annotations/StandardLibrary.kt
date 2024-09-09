// This is a hack to access internal annotations of the Kotlin compiler.
// These annotations are used to *restrict* type inference, so if they
// are ever removed, this won't break existing code.

// For more information, see https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/src/kotlin/internal/Annotations.kt#L19

@file:Suppress("PackageDirectoryMismatch")

package kotlin.internal

@Target(AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
internal annotation class OnlyInputTypes
