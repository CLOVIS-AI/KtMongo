/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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
	alias(libsCommon.plugins.testBalloon)
	alias(libsCommon.plugins.kotlinx.serialization)
	id("dev.opensavvy.ktmongo.build.templator")
}

kotlin {
	jvm()
	js {
		browser()
		nodejs()
	}
	// linuxX64() // TODO https://youtrack.jetbrains.com/issue/KT-83308 Kotlin Native currently crashes on our codebase
	// linuxArm64()
	// macosX64()
	// macosArm64()
	// iosArm64()
	// iosX64()
	// iosSimulatorArm64()
	// watchosX64()
	// watchosArm32()
	// watchosArm64()
	// watchosSimulatorArm64()
	// tvosX64()
	// tvosArm64()
	// tvosSimulatorArm64()
	// mingwX64()
	// wasmJs { // TODO Wasm crashes with a StackOverflow error
	// 	browser()
	// nodejs()
	// }
	// wasmWasi {
	// 	nodejs()
	// }

	sourceSets.commonMain.dependencies {
		api(projects.bson)
		api(projects.annotations)
		implementation(libsCommon.jetbrains.annotations)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.kotest.assertions)
		implementation(libsCommon.kotlin.test)
		implementation(libs.kotlinx.serialization)
		implementation(libs.mongodb.kotlin.reflection)
	}

	sourceSets.jvmTest.dependencies {
		// Test the official driver with or without reflection
		implementation(projects.bsonOfficial)
		implementation(libs.mongodb.sync.jvm)
		implementation(libs.mongodb.kotlinx.serialization)
		implementation(libs.mongodb.kotlin.reflection)
		implementation(libs.kotlinx.serialization)
	}
}

library {
	name.set("KtMongo: MongoDB request DSL")
	description.set("Kotlin-first DSL for writing expressive and typesafe MongoDB queries")
	homeUrl.set("https://ktmongo.opensavvy.dev")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}

	coverage.set(70)
}
