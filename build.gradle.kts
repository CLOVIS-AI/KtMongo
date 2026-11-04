import org.jetbrains.kotlin.gradle.targets.js.npm.LockFileMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmExtension
import org.jetbrains.kotlin.gradle.targets.wasm.npm.WasmNpmExtension

/*
 * This is the root project.
 *
 * The root project should remain empty. Code, etc, should happen in subprojects, and the root project should only delegate
 * work to subprojects.
 *
 * In particular, *avoid using the 'allprojects' and 'subprojects' Gradle blocks!* They slow down the configuration phase.
 */

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.root)

	// Some plugins *must* be configured on the root project.
	// In these cases, we explicitly tell Gradle not to apply them.
	alias(libsCommon.plugins.kotlin) apply false
	alias(libsCommon.plugins.kotlin.jvm) apply false
	alias(libsCommon.plugins.ksp) apply false
	alias(libsCommon.plugins.kotlinx.serialization) apply false
	alias(libsCommon.plugins.kotlinx.powerAssert) apply false
}

dependencies {
	// List the 'library' projects
	// library(projects.core)
}

// region Check the users of the project didn't forget to rename the group

val projectPath: String? = System.getenv("CI_PROJECT_PATH")
if (projectPath != null && projectPath != "opensavvy/playgrounds/gradle" && group == "dev.opensavvy.playground") {
	error("The project is declared to be in the group '$group', which is recognized as the Gradle Playground, but it's hosted in '$projectPath', which is not the Playground. Maybe you forgot to rename the group when importing the Playground in your own project?")
}

// endregion
// region Enable the :lib template if we're in the playground

if (group == "dev.opensavvy.playground") {
	dependencies {
		library(projects.gradle.templates.templateLib)
	}
}

// endregion
// region Always ignore the package-lock.json file
// See https://kotlinlang.org/docs/js-project-setup.html#reporting-that-yarn-lock-has-been-updated

plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
	the<NpmExtension>().packageLockMismatchReport.set(LockFileMismatchReport.NONE)
	the<NpmExtension>().packageLockAutoReplace.set(true)
	the<NpmExtension>().override("webpack", libsCommon.versions.webpack.get())
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsRootPlugin> {
	the<WasmNpmExtension>().packageLockMismatchReport.set(LockFileMismatchReport.NONE)
	the<WasmNpmExtension>().packageLockAutoReplace.set(true)
	the<WasmNpmExtension>().override("webpack", libsCommon.versions.webpack.get())
}

// endregion
// region Configure Dokka overrides

dokka {
	pluginsConfiguration.html {
		templatesDir.set(file("docs/dokka/overrides"))
	}
}

// endregion
// region NodeJS version

allprojects {
	plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
		the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().version.set(libsCommon.versions.nodejs)
	}

	plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin> {
		the<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec>().version.set(libsCommon.versions.nodejs)
	}
}

// endregion
