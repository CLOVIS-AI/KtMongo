plugins {
	id("conventions.base")
	id("conventions.library")
}

dependencies {
	implementation(libs.mongodb.bson)

	testImplementation(libs.bundles.kotest)
}

kotlin {
	compilerOptions.freeCompilerArgs.add("-Xallow-kotlin-package")
}
