package opensavvy.playground.core

import opensavvy.prepared.runner.testballoon.preparedSuite

val PlaygroundVerifyTestBalloonCompat by preparedSuite {

	test("Simplest possible test") {
		@Suppress("SimplifyBooleanWithConstants") // The compiler knows this test will always succeed
		check("Hello World!" == message)
	}

	suite("Verify that unicode is supported in suite names: \u2606") {
		test("Verify that unicode is supported in test names: \u2606\u2606\u2606\u2606") {
			// This test verifies that our CI is able to handle Unicode characters in test names.
			// See https://gitlab.com/gitlab-org/gitlab/-/issues/580885
		}
	}
}
