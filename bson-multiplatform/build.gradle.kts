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
		browser()
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
	wasmJs {
		browser()
		nodejs()
	}
	wasmWasi {
		nodejs()
	}

	sourceSets.commonMain.dependencies {
		api(projects.bson)
		api(libs.kotlinx.io)
		api(libs.kotlinx.serialization)
	}

	sourceSets.commonTest.dependencies {
		implementation(projects.bsonTests)
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.kotlin.test)
	}
}

library {
	name.set("Kotlin BSON • Pure Kotlin Multiplatform implementation")
	description.set("Kotlin-first BSON library implemented in pure Kotlin code")
	homeUrl.set("https://ktmongo.opensavvy.dev")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}

	coverage.set(75)
}

// region Workaround KT-86874
// https://youtrack.jetbrains.com/issue/KT-86874/WASI-Test-running-process-exited-unexpectedly

// KGP 2.4.0 generates a WASM test runner that fails to deliver all test output.
// Root cause: Node.js ESM init sets O_NONBLOCK on fd 1 (stdout pipe). WASI's fd_write
// calls uv_fs_write(NULL,...) directly on fd 1, so writes return EAGAIN when KGP's
// 1 MB pipe buffer is full, silently dropping the remaining ~1.4 MB of output.
// Fix: reset fd 1 to blocking mode before wasi.start() so fd_write blocks until
// the pipe drains, ensuring all 2.5 MB of test output is delivered.
// returnOnExit: true is kept so proc_exit(0) unwinds via exception rather than
// calling process.exit() mid-execution.
val patchWasmWasiTestMjs by tasks.registering {
	description = "Workaround KT-86874 by patching the generated JS launcher for WASI"

	dependsOn("compileTestDevelopmentExecutableKotlinWasmWasi")

	val kotlinDir = layout.buildDirectory
		.dir("compileSync/wasmWasi/test/testDevelopmentExecutable/kotlin")

	doLast {
		val dir = kotlinDir.get().asFile

		val mainMjs = dir.resolve("KtMongo-bson-multiplatform-test.mjs")
		if (mainMjs.exists()) {
			mainMjs.writeText(
				mainMjs.readText()
					.replace(
						"new WASI({ version: 'preview1', args: argv, env, })",
						"new WASI({ version: 'preview1', args: argv, env, returnOnExit: true })",
					)
					.replace(
						"\nwasi.start(wasmInstance);",
						"\nif (typeof process.stdout._handle?.setBlocking === 'function') process.stdout._handle.setBlocking(true);\nwasi.start(wasmInstance);",
					),
			)
		}
	}
}

tasks.named("wasmWasiNodeTest") {
	dependsOn(patchWasmWasiTestMjs)
}

// endregion
