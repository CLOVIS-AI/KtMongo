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

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
}

kotlin {
	jvm()

	sourceSets.commonMain.dependencies {
		api(projects.dsl)
		api(projects.bsonOfficial)
	}

	sourceSets.jvmMain.dependencies {
		api(libs.mongodb.core.jvm)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.kotlin.test)
		implementation(libsCommon.kotest.assertions)
	}
}

library {
	name.set("Shared utilities for the sync- and coroutines-based KtMongo drivers based on the official Kotlin driver")
	description.set("This is an intermediate dependency of the driver-sync and driver-coroutines libraries. Users should not need to interact with this artifact directly.")
	homeUrl.set("https://ktmongo.opensavvy.dev")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
