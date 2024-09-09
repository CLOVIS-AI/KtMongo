plugins {
	id("conventions.base")
	id("conventions.library")
}

dependencies {
	api(projects.dsl)
	api(libs.kotlinx.coroutines)

	api(libs.mongodb.coroutines)
}
