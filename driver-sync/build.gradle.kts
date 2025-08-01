/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

	sourceSets.commonMain.dependencies {
		api(projects.dsl)
		implementation(projects.driverSharedOfficial)
	}

	sourceSets.jvmMain.dependencies {
		api(libs.mongodb.sync.jvm)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.kotest.assertions)
	}
}

library {
	name.set("MongoDB driver for Kotlin (synchronous)")
	description.set("Kotlin-first MongoDB driver")
	homeUrl.set("https://opensavvy.gitlab.io/ktmongo/docs")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
