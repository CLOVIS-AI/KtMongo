package opensavvy.playground.core

import io.kotest.core.spec.style.FunSpec
import opensavvy.prepared.runner.kotest.PreparedSpec

class HelloWorldKotestTest : PreparedSpec({

	test("Hello world!") {
		@Suppress("SimplifyBooleanWithConstants") // The compiler knows this test will always succeed
		check("Hello World!" == message)
	}

	test("Yay • \u2606\u2606\u2606\u2606 Unicode!") {
		// This test verifies that our CI is able to handle Unicode characters in test names.
		// See https://gitlab.com/gitlab-org/gitlab/-/issues/580885
	}
})

class FooTest : FunSpec({
	test("test") {
		check(true)
	}
})
