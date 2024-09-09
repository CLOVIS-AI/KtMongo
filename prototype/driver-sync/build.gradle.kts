plugins {
	id("conventions.base")
	id("conventions.library")
}

dependencies {
	api(projects.dsl)

	api(libs.mongodb.sync)
}
