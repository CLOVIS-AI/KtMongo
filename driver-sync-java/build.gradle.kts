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
	alias(opensavvyConventions.plugins.kotlin.abstractLibrary)
	java
	kotlin("jvm")
	alias(libsCommon.plugins.testBalloon)
}

kotlin {
	jvmToolchain(opensavvyConventions.versions.java.compat.get().toInt())
}

dependencies {
	api(projects.driverSync)
	api(libs.java.accessLambdaName)
	testImplementation(libs.junit.jupiter.api)
	testImplementation(libs.junit.jupiter.engine)
	testImplementation(libs.junit.platform.launcher)
	testImplementation(libsCommon.opensavvy.prepared.testBalloon)
	testImplementation(libsCommon.kotest.assertions)
}

library {
	name.set("MongoDB driver for Kotlin (synchronous, with Java helpers)")
	description.set("Modern MongoDB driver for Kotlin and Java")
	homeUrl.set("https://ktmongo.opensavvy.dev")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
