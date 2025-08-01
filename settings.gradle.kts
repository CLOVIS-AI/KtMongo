/*
 * Copyright (c) 2023-2025, OpenSavvy and contributors.
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

/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/8.1.1/userguide/multi_project_builds.html
 */

rootProject.name = "KtMongo"

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	repositories {
		mavenCentral()
	}

	versionCatalogs {
		create("libsCommon") {
			from(files("gradle/common.versions.toml"))
		}
	}
}

pluginManagement {
	repositories {
		// region OpenSavvy Conventions

		maven {
			name = "opensavvy-gradle-conventions"
			url = uri("https://gitlab.com/api/v4/projects/51233470/packages/maven")

			metadataSources {
				gradleMetadata()
				mavenPom()
			}

			content {
				@Suppress("UnstableApiUsage")
				includeGroupAndSubgroups("dev.opensavvy")
			}
		}

		// endregion
		// region Standard repositories

		gradlePluginPortal()
		google()
		mavenCentral()

		// endregion
	}

	includeBuild("gradle/conventions")
}

plugins {
	id("dev.opensavvy.conventions.settings") version "2.1.3"
}

include(
	"annotations",

	"bson",
	"bson-official",
	"bson-multiplatform",
	"bson-tests",

	"dsl",

	"driver-shared-official",
	"driver-sync",
	"driver-sync-java",
	"driver-coroutines",

	"test",

	"docs:website",
	"gradle:templates:template-app",
	"gradle:templates:template-lib",
)
