package fr.qsh.ktmongo.dsl.path

import fr.qsh.ktmongo.dsl.LowLevelApi
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KProperty1

@Suppress("unused")
@OptIn(LowLevelApi::class)
class PropertyPathTest : FunSpec({
	class Profile(
		val name: String,
		val age: Int,
	)

	class Friend(
		val userId: String,
		val name: String,
	)

	class User(
		val id: Int,
		val profile: Profile,
		val friends: List<Friend>,
	)

	class PropertySyntaxTestScope : PropertySyntaxScope {

		// force 'User' to ensure all functions keep the User as the root type
		infix fun KProperty1<User, *>.shouldHavePath(path: String) =
			this.path().toString() shouldBe path

	}

	context("Field access") {
		test("Root field") {
			with(PropertySyntaxTestScope()) {
				User::id shouldHavePath "id"
			}
		}

		test("Nested field") {
			with(PropertySyntaxTestScope()) {
				User::profile / Profile::name shouldHavePath "profile.name"
				User::profile / Profile::age shouldHavePath "profile.age"
			}
		}
	}

	context("Indexed access") {
		test("Indexed object") {
			with(PropertySyntaxTestScope()) {
				User::friends[0] shouldHavePath "friends.$0"
			}
		}

		test("Indexed nested field") {
			with(PropertySyntaxTestScope()) {
				User::friends[0] / Friend::name shouldHavePath "friends.$0.name"
			}
		}
	}
})
