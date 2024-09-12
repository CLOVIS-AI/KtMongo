/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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
	js {
		nodejs()
	}

	sourceSets.commonMain.dependencies {
		api(projects.bson)
	}

	sourceSets.commonTest.dependencies {
		implementation(libs.prepared)
		implementation(opensavvyConventions.aligned.kotlin.test)
	}
}

library {
	name.set("MongoDB query DSL")
	description.set("Kotlin-first DSL for writing expressive and typesafe MongoDB queries")
	homeUrl.set("https://gitlab.com/opensavvy/ktmongo")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
