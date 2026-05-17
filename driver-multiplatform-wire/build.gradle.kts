/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
	alias(libsCommon.plugins.kotlinx.serialization)
	alias(libsCommon.plugins.testBalloon)
}

kotlin {
	jvm()
	js {
		nodejs()
	}
	linuxX64()
	linuxArm64()
	macosArm64()
	iosArm64()
	iosSimulatorArm64()
	watchosArm32()
	watchosArm64()
	watchosSimulatorArm64()
	tvosArm64()
	tvosSimulatorArm64()
	mingwX64()
	// wasmJs {
	// 	nodejs()
	// }

	sourceSets.commonMain.dependencies {
		api(projects.bson)
		api(projects.bsonMultiplatform)
		api(libs.kotlinx.io)
		api(libs.kotlinx.coroutines)

		implementation(libs.ktor.network)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.kotlin.test)
	}
}

library {
	name.set("KtMongo: MongoDB driver for Kotlin • Multiplatform wire protocol")
	description.set("Low-level pure Kotlin implementation of the MongoDB wire protocol")
	homeUrl.set("https://ktmongo.opensavvy.dev")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}

	coverage.set(45) // TODO: increase coverage when stabilizing
}
