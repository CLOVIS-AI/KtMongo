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
	alias(opensavvyConventions.plugins.kotlin.internal)
	alias(opensavvyConventions.plugins.aligned.kotlinx.serialization)
}

kotlin {
	jvm()
	js {
		nodejs()
	}

	sourceSets.commonMain.dependencies {
		api(projects.driverSync)
		api(projects.driverCoroutines)

		api(libs.prepared)

		api(libs.kotlinx.serialization)
	}

	sourceSets.jvmMain.dependencies {
		api(libs.mongodb.kotlinx.serialization)
	}
}
