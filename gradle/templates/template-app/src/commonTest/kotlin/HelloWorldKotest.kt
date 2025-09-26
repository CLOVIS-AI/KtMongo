package opensavvy.playground.core

import opensavvy.prepared.runner.kotest.PreparedSpec

class HelloWorldKotestTest : PreparedSpec({

	test("Hello world!") {
		@Suppress("SimplifyBooleanWithConstants") // The compiler knows this test will always succeed
		check("Hello World!" == message)
	}
})
