@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.application)
	alias(libsCommon.plugins.testBalloon)
}

kotlin {
	jvm {
		binaries {
			executable {
				mainClass.set("opensavvy.playground.app.MainKt")
			}
		}
	}
	js {
		browser()
		nodejs()
	}
	linuxX64()
	linuxArm64()
	macosArm64()
	iosArm64()
	iosSimulatorArm64()
	watchosArm64()
	watchosSimulatorArm64()
	tvosArm64()
	tvosSimulatorArm64()
	mingwX64()
	wasmJs {
		browser()
		nodejs()
	}

	sourceSets.commonMain.dependencies {
		implementation(projects.gradle.templates.templateLib)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.bundles.testBalloon)
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

		if (this is AbstractTestTask) {
			onlyIf("The template tests do not need to run when not building as part of the Playground") { System.getenv("CI") != null }
		}
	}
}

// endregion
