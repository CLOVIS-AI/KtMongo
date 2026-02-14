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
	alias(libsCommon.plugins.kotlinx.serialization)
	alias(libsCommon.plugins.testBalloon)
}

kotlin {
	jvm()

	compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

	sourceSets.commonMain.dependencies {
		api(projects.bson)
	}

	sourceSets.jvmMain.dependencies {
		api(libs.mongodb.bson.jvm)
	}

	sourceSets.commonTest.dependencies {
		implementation(projects.bsonTests)
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.kotest.assertions)
		implementation(libsCommon.kotlin.test)
	}

	sourceSets.jvmTest.dependencies {
		implementation(libs.mongodb.sync.jvm)
		implementation(libs.mongodb.kotlinx.serialization)
		implementation(libs.kotlinx.serialization)
	}
}

library {
	name.set("Kotlin BSON • Based on the official MongoDB implementation")
	description.set("Kotlin-first BSON library based on the official BSON libraries")
	homeUrl.set("https://ktmongo.opensavvy.dev")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
