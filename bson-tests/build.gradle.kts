/*
 * Copyright (c) 2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
	alias(libsCommon.plugins.testBalloon)
	alias(libsCommon.plugins.kotlinx.serialization)
}

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

	sourceSets.commonMain.dependencies {
		api(projects.bson)
		api(libsCommon.opensavvy.prepared)
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.kotlin.test)
		implementation(libsCommon.jetbrains.annotations)
		implementation(libs.kotlinx.serialization)
	}
}

library {
	name.set("Kotlin BSON • Test suites")
	description.set("Test suites for validating a BSON implementation on all platforms supported by Kotlin")
	homeUrl.set("https://ktmongo.opensavvy.dev")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}

powerAssert {
	functions = listOf("kotlin.check")
	includedSourceSets = listOf("commonMain", "jvmMain", "jsMain", "iosArm64Main", "iosSimulatorArm64Main", "iosX64Main", "linuxArm64Main", "linuxX64Main", "macosArm64Main", "macosX64Main", "mingwX64Main", "tvosArm64Main", "tvosSimulatorArm64Main", "tvosX64Main", "wasmJsMain", "watchosArm32Main", "watchosArm64Main", "watchosSimulatorArm64Main", "watchosX64Main")
}
