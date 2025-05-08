plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.application)
}

kotlin {
	jvm {
		binaries {
			executable {
				mainClass.set("opensavvy.playground.app.MainKt")
			}
		}
	}

	sourceSets.commonMain.dependencies {
		implementation(projects.gradle.templates.templateLib)
	}
}
