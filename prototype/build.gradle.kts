
plugins {
	alias(libs.plugins.kotlin) apply false
	alias(libs.plugins.dokkatoo)
}

dependencies {
	dokkatoo(projects.dsl)
	dokkatoo(projects.driverSync)
	dokkatoo(projects.driverCoroutines)
}
