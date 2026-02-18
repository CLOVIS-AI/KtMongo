package opensavvy.playground.core

import opensavvy.prepared.runner.testballoon.preparedSuite

val HelloWorldTestBalloonTest by preparedSuite {

	test("Hello world!") {
		@Suppress("SimplifyBooleanWithConstants") // The compiler knows this test will always succeed
		check("Hello World!" == message)
	}

	suite("Nested suite with Unicode \u2606") {
		test("Yay • \u2606\u2606\u2606\u2606 Unicode!") {
			// This test verifies that our CI is able to handle Unicode characters in test names.
			// See https://gitlab.com/gitlab-org/gitlab/-/issues/580885
		}
	}
}
