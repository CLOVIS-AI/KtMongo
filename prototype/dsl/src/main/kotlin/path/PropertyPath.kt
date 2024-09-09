package fr.qsh.ktmongo.dsl.path

import fr.qsh.ktmongo.dsl.LowLevelApi
import kotlin.reflect.*

/**
 * Helper to construct a [Path] from Kotlin property names.
 *
 * This class is masquerading as an instance of [KProperty1]. This allows us to refer to fields
 * directly using [property references](https://kotlinlang.org/docs/reflection.html#bound-function-and-property-references).
 *
 * Instances of this class are created using the helpers [div].
 *
 * @param RootParent The type at the root of this path.
 * Normally, [KProperty1] instances refer to the direct parent.
 * Instead, this class refers to the very first type in the property chain.
 * @param Value The type of the property pointed to by this instance.
 */
@LowLevelApi
@Suppress("NO_REFLECTION_IN_CLASS_PATH") // None of the functions are called by our code. The caller is responsible for fixing this.
private class PropertyPath<RootParent, Value>(
	/**
	 * The path of this property.
	 *
	 * This is *not* the path to the parent property, but to the current property.
	 *
	 * ### Example
	 *
	 * The path for `User::foo / Foo::id` is `"foo.id"`.
	 */
	internal val path: Path,

	/**
	 * The property which this path represents, and masquerades as.
	 */
	private val backingProperty: KProperty1<*, *>,
) : KProperty1<RootParent, Value> {

	// We just delegate everything to the backing property.
	// We cannot use interface delegation because we do not know the
	// type parameters of the 'self' property :(

	override val annotations: List<Annotation> get() = backingProperty.annotations
	override val getter: KProperty1.Getter<RootParent, Value> get() = throw UnsupportedOperationException("Calling 'getter' on a PropertyPath is not supported")
	override val isAbstract: Boolean get() = backingProperty.isAbstract
	override val isConst: Boolean get() = backingProperty.isConst
	override val isFinal: Boolean get() = backingProperty.isFinal
	override val isLateinit: Boolean get() = backingProperty.isLateinit
	override val isOpen: Boolean get() = backingProperty.isOpen
	override val isSuspend: Boolean get() = backingProperty.isSuspend
	override val name: String get() = backingProperty.name
	override val parameters: List<KParameter> get() = backingProperty.parameters
	override val returnType: KType get() = backingProperty.returnType
	override val typeParameters: List<KTypeParameter> get() = backingProperty.typeParameters
	override val visibility: KVisibility? get() = backingProperty.visibility

	override fun call(vararg args: Any?): Value = throw UnsupportedOperationException("Calling 'call' on a PropertyPath is not supported")

	override fun callBy(args: Map<KParameter, Any?>): Value = throw UnsupportedOperationException("Calling 'callBy' on a PropertyPath is not supported")

	override fun invoke(p1: RootParent): Value = throw UnsupportedOperationException("Calling 'invoke' on a PropertyPath is not supported")

	override fun getDelegate(receiver: RootParent) = throw UnsupportedOperationException("Calling 'getDelegate' on a PropertyPath is not supported")

	override fun get(receiver: RootParent): Value = throw UnsupportedOperationException("Calling 'get' on a PropertyPath is not supported")

	override fun toString() = path.toString()
}

interface PropertySyntaxScope {

	@LowLevelApi
	fun KProperty1<*, *>.path() =
		if (this is PropertyPath<*, *>) this.path
		else Path.root(PathSegment.Field(this.name))

	/**
	 * Combines Kotlin properties into a path usable to point to a specific field in a document.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val id: Int,
	 *     val profile: Profile,
	 * )
	 *
	 * class Profile(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * // Refer to the id
	 * println(User::id)
	 * // → 'id'
	 *
	 * // Refer to the name
	 * println(User::profile / Profile::name)
	 * // → 'profile.name'
	 *
	 * // Refer to the name
	 * println(User::profile / Profile::age)
	 * // → 'profile.age'
	 * ```
	 *
	 * @see get Indexed access (`.$0.`)
	 */
	@OptIn(LowLevelApi::class)
	operator fun <T0, T1, T2> KProperty1<T0, T1>.div(child: KProperty1<T1, T2>): KProperty1<T0, T2> =
		PropertyPath(
			path = this.path() + PathSegment.Field(child.name),
			backingProperty = child,
		)

	/**
	 * Denotes a specific item in an array, by index.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val friends: List<Friend>,
	 * )
	 *
	 * class Friend(
	 *     val name: String,
	 * )
	 *
	 * // Refer to the first friend
	 * println(User::friends[0])
	 * // → 'friends.$0'
	 *
	 * // Refer to the third friend's name
	 * println(User::friends[2] / Friend::name)
	 * // → 'friends.$0.name'
	 * ```
	 *
	 * @see div Access based on the field name (`.foo.`)
	 */
	@OptIn(LowLevelApi::class)
	operator fun <T0, T1> KProperty1<T0, Collection<T1>>.get(index: Int): KProperty1<T0, T1> =
		PropertyPath(
			path = this.path() + PathSegment.Indexed(index),
			backingProperty = this,
		)

}
