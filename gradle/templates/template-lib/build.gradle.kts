import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
	jvm()
	js {
		browser()
		nodejs()
	}
	linuxX64()
	linuxArm64()
	macosX64()
	macosArm64()
	iosArm64()
	iosX64()
	iosSimulatorArm64()
	watchosX64()
	watchosArm32()
	watchosArm64()
	watchosSimulatorArm64()
	tvosX64()
	tvosArm64()
	tvosSimulatorArm64()
	mingwX64()
	wasmJs {
		browser()
		nodejs()
	}
	wasmWasi {
		nodejs()
	}

	sourceSets.commonTest.dependencies {
		implementation(opensavvyConventions.aligned.kotlin.test)
	}

	sourceSets.jvmTest.dependencies {
		implementation(opensavvyConventions.aligned.kotlin.test.junit5)
	}
}

library {
	name.set("Playground Core")
	description.set("Project template with configured MavenCentral publication")
	homeUrl.set("https://gitlab.com/opensavvy/playgrounds/gradle")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}

// region Publication test
// Even though this module is included in all repositories that import the Playground, we
// don't want to always publish this template.

val appGroup: String? by project

@Suppress("UnstableApiUsage") // 'onlyIf' is unstable
if (appGroup != "dev.opensavvy.playground") {
	tasks.configureEach {
		if (name.startsWith("publish")) {
			onlyIf("Publishing is only enabled when built as part of the Playground") { false }
		}

		if (this is Test) {
			onlyIf("The template tests do not need to run when not building as part of the Playground") { System.getenv("CI") != null }
		}
	}
}

// endregion
