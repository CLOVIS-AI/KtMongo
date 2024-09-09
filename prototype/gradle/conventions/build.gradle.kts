plugins {
	`kotlin-dsl`
}

kotlin {
	jvmToolchain(11)
}

dependencies {
	implementation(libs.gradle.kotlin)
	implementation(libs.gradle.dokkatoo)
}
