package opensavvy.playground.core

import io.kotest.core.spec.style.FunSpec
import opensavvy.prepared.runner.kotest.PreparedSpec

class PlaygroundVerifyKotestAndPreparedCompat : PreparedSpec({

	test("Simplest possible test") {
		@Suppress("SimplifyBooleanWithConstants") // The compiler knows this test will always succeed
		check("Hello World!" == message)
	}

	test("Verify that unicode is supported in test names: \u2606\u2606\u2606\u2606") {
		// This test verifies that our CI is able to handle Unicode characters in test names.
		// See https://gitlab.com/gitlab-org/gitlab/-/issues/580885
	}
})

class PlaygroundVerifyKotestCompat : FunSpec({
	test("Simplest possible test") {
		check(true)
	}
})
