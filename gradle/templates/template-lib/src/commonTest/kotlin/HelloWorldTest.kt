package opensavvy.playground.core

import opensavvy.prepared.runner.testballoon.preparedSuite

val HelloWorldTest by preparedSuite {

	test("Hello world!") {
		@Suppress("SimplifyBooleanWithConstants") // The compiler knows this test will always succeed
		check("Hello World!" == message)
	}
}
