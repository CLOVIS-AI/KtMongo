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

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
	alias(libsCommon.plugins.testBalloon)
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
	// @OptIn(ExperimentalWasmDsl::class)
	// wasmJs {
	// 	browser()
	// 	nodejs()
	// }

	sourceSets.commonMain.dependencies {
		api(projects.bson)
		api(libs.kotlinx.io)
	}

	sourceSets.commonTest.dependencies {
		implementation(projects.bsonTests)
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.kotest.assertions)
	}
}

library {
	name.set("Kotlin BSON • Pure Kotlin Multiplatform implementation")
	description.set("Kotlin-first BSON library implemented in pure Kotlin code")
	homeUrl.set("https://opensavvy.gitlab.io/ktmongo/docs")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}

	coverage.set(80)
}
