plugins {
	id("conventions.base")

	alias(libs.plugins.kotlin)
	alias(libs.plugins.dokkatoo)
}

dependencies {
	implementation(projects.driverSync)
}
