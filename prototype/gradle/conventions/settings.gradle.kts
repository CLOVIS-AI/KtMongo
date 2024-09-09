rootProject.name = "conventions"

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}

	versionCatalogs {
		create("libs") {
			from(files("../libs.versions.toml"))
		}
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
