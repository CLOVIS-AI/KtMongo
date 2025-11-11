@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.application)
	alias(libsCommon.plugins.kotest)
	alias(libsCommon.plugins.ksp)
}

kotlin {
	jvm {
		binaries {
			executable {
				mainClass.set("opensavvy.playground.app.MainKt")
			}
		}
	}
	js {
		browser()
		nodejs()
	}
	linuxX64()
	linuxArm64()
	macosX64()
	macosArm64()
	iosArm64()
	iosX64()
	iosSimulatorArm64()
	watchosX64()
	watchosArm32()
	watchosArm64()
	watchosSimulatorArm64()
	tvosX64()
	tvosArm64()
	tvosSimulatorArm64()
	mingwX64()
	wasmJs {
		browser()
		nodejs()
	}

	sourceSets.commonMain.dependencies {
		implementation(projects.gradle.templates.templateLib)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.kotest)
	}
}
