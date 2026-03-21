/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

rootProject.name = "conventions"

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}

	versionCatalogs {
		create("libs") {
			from(files("../libs.versions.toml"))
		}
		create("libsCommon") {
			from(files("../common.versions.toml"))
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
}

plugins {
	id("dev.opensavvy.conventions.settings") version "2.3.5"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
	"dsl-templator",
)
