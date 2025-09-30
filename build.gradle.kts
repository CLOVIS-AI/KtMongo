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

import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

/*
 * This is the root project.
 *
 * The root project should remain empty. Code, etc, should happen in subprojects, and the root project should only delegate
 * work to subprojects.
 *
 * In particular, *avoid using the 'allprojects' and 'subprojects' Gradle blocks!* They slow down the configuration phase.
 */

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.root)

	// Some plugins *must* be configured on the root project.
	// In these cases, we explicitly tell Gradle not to apply them.
	alias(libsCommon.plugins.kotlin) apply false
	alias(libsCommon.plugins.kotlin.jvm) apply false
	alias(libsCommon.plugins.ksp) apply false
	alias(libsCommon.plugins.kotlinx.serialization) apply false
	alias(libsCommon.plugins.kotlinx.powerAssert) apply false
}

dependencies {
	// List the 'library' projects
	library(projects.annotations)
	library(projects.bson)
	library(projects.bsonOfficial)
	library(projects.bsonMultiplatform)
	library(projects.bsonTests)
	library(projects.dsl)
	library(projects.driverSharedOfficial)
	library(projects.driverSync)
	library(projects.driverSyncJava)
	library(projects.driverSyncKmongo)
	library(projects.driverCoroutines)
	library(projects.driverCoroutinesKmongo)
}

// region Check the users of the project didn't forget to rename the group

val projectPath: String? = System.getenv("CI_PROJECT_PATH")
if (projectPath != null && projectPath != "opensavvy/playgrounds/gradle" && group == "dev.opensavvy.playground") {
	error("The project is declared to be in the group '$group', which is recognized as the Gradle Playground, but it's hosted in '$projectPath', which is not the Playground. Maybe you forgot to rename the group when importing the Playground in your own project?")
}

// endregion
// region Enable the :lib template if we're in the playground

if (group == "dev.opensavvy.playground") {
	dependencies {
		library(projects.gradle.templates.templateLib)
	}
}

// endregion
// region Always ignore the yarn.lock file
// See https://kotlinlang.org/docs/js-project-setup.html#reporting-that-yarn-lock-has-been-updated

plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
	the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.NONE
	the<YarnRootExtension>().yarnLockAutoReplace = true
}

// endregion
