package fr.qsh.ktmongo.dsl.path

import fr.qsh.ktmongo.dsl.LowLevelApi
import fr.qsh.ktmongo.dsl.path.PathSegment.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("unused")
@OptIn(LowLevelApi::class)
class PathTest : FunSpec({
	test("Root field") {
		Path.root(Field("test")).toString() shouldBe "test"
	}

	test("Nested field") {
		(Path.root(Field("test")) + Field("bar")).toString() shouldBe "test.bar"
	}

	test("Deeper nested field") {
		(Path.root(Field("test")) + Field("bar") + Field("foo")).toString() shouldBe "test.bar.foo"
	}

	test("Indexed") {
		(Path.root(Field("test")) + Indexed(3) + Field("bar")).toString() shouldBe "test.$3.bar"
	}

	test("Positional") {
		(Path.root(Field("test")) + Positional + Field("bar")).toString() shouldBe "test.$.bar"
	}

	test("All positional") {
		(Path.root(Field("test")) + AllPositional + Field("bar")).toString() shouldBe "test.$[].bar"
	}
})
