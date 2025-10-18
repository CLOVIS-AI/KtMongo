#!/usr/bin/env kotlin

/*
 * Copyright (c) 2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// https://gitlab-ci-kts.opensavvy.dev/news/index.html
@file:DependsOn("dev.opensavvy.gitlab:gitlab-ci-kotlin-jvm:0.7.1")

import opensavvy.gitlab.ci.*
import opensavvy.gitlab.ci.Environment.EnvironmentTier.Development
import opensavvy.gitlab.ci.plugins.Gradle.Companion.gradlew
import opensavvy.gitlab.ci.plugins.Gradle.Companion.useGradle
import opensavvy.gitlab.ci.script.shell

/**
 * The version of images downloaded from https://gitlab.com/opensavvy/automation/containers
 */
val ciContainers = "0.8.1"

/**
 * The URL of the website built by /docs/website.
 */
val siteUrl = "\$CI_PAGES_URL"

// ***

fun Job.opensavvyImage(name: String) =
	image("registry.gitlab.com/opensavvy/automation/containers/$name", ciContainers)

// region Kotlin Multiplatform

fun Job.jvm() {
	useGradle()
	opensavvyImage("java")
}

fun Job.jsBrowser() {
	useGradle()
	opensavvyImage("chromium")
}

fun Job.jsNode() {
	useGradle()
	opensavvyImage("nodejs")
}

fun Job.nativeLinuxX64() {
	useGradle()
	opensavvyImage("java")
}

fun Job.nativeIosArm64() {
	useGradle()
	image("macos-15-xcode-16")

	beforeScript {
		shell("xcodebuild -downloadPlatform iOS")
	}

	tag("saas-macos-medium-m1")

	retry(1) {
		onExitCode(1)
	}
}

// endregion

val supportedMongoDB = listOf("6.0.21", "7.0.18", "8.0.6")

gitlabCi {
	val build by stage()
	val test by stage()
	val deploy by stage()

	// region Tests

	for (mongo in supportedMongoDB) {
		val checkJvm by job(stage = test) {
			jvm()

			service("mongo", mongo) {
				alias = "mongo"
			}

			script {
				gradlew.tasks(
					"check :koverLog :koverHtmlReport koverVerify --rerun",
					"-x jsBrowserTest",
					"-x jsNodeTest",
					"-x wasmJsBrowserTest",
					"-x wasmJsNodeTest",
					"-x wasmWasiNodeTest",
					"-x linuxX64Test",
					"-x mingwX64Test",
					"-x macosX64Test",
					"-x macosArm64Test",
					"-x iosX64Test",
					"-x iosSimulatorArm64Test",
					"-x watchosX64Test",
					"-x tvosX64Test",
					$$"-PappVersion=$project_version",
				)
			}

			afterScript {
				shell("mkdir -p jvm-cover-report")
				shell("mv build/reports/kover/html/* jvm-cover-report")
			}

			artifacts {
				include("jvm-cover-report")
				exposeAs("JVM code coverage")
			}

			interruptible(true)
		}
	}

	val checkJsBrowser by job(stage = test) {
		jsBrowser()

		script {
			gradlew.tasks(
				"jsBrowserTest wasmJsBrowserTest",
				$$"-PappVersion=$project_version",
			)
		}

		interruptible(true)
	}

	val checkJsNode by job(stage = test) {
		jsNode()

		script {
			gradlew.tasks(
				"jsNodeTest wasmJsNodeTest wasmWasiNodeTest",
				$$"-PappVersion=$project_version",
			)
		}

		interruptible(true)
	}

	val checkLinuxX64 by job(stage = test) {
		nativeLinuxX64()

		script {
			gradlew.tasks(
				"linuxX64Test mingwX64Test",
				$$"-PappVersion=$project_version",
			)
		}

		interruptible(true)
	}

	val checkIosArm64 by job(stage = test) {
		nativeIosArm64()

		script {
			gradlew.tasks(
				"iosSimulatorArm64Test watchosSimulatorArm64Test",
				$$"-PappVersion=$project_version",
			)
		}

		interruptible(true)
	}

	// endregion
	// region Documentation

	val mkdocs by job(stage = build) {
		opensavvyImage("mkdocs")
		variable("GIT_DEPTH", "0")

		beforeScript {
			shell("./docs/website/verify-marker.sh")
			shell($$"./gradlew docs:website:embedDokkaIntoMkDocs -PappVersion=$project_version")
			shell("cd docs/website")
			shell("ls")
			shell($$"""echo "repo_url: $CI_PROJECT_URL">>mkdocs.yml""")
			shell($$"""echo "repo_name: $CI_PROJECT_TITLE">>mkdocs.yml""")
			shell($$"""echo "site_url: $$siteUrl">>mkdocs.yml""")
		}

		script {
			shell("./generate-news.sh")
			shell("mkdocs build --site-dir ../../docs-website")
		}

		afterScript {
			shell("""echo "URL=$(.gitlab/ci/review-url.sh docs-website/index.html)">>docs.env""")
		}

		artifacts {
			include("docs-website")
			dotenv("docs.env")
		}

		environment {
			name("review/${Variable.Commit.Ref.slug}/docs")
			url($$"$URL")
			tier(Development)
		}

		interruptible(true)
	}

	val dokka by job(stage = build) {
		jvm()

		script {
			gradlew.tasks(
				":dokkaGeneratePublicationHtml",
				$$"-PappVersion=$project_version"
			)
		}

		afterScript {
			shell("mkdir -p api-docs")
			shell("mv build/dokka/html/* api-docs")
			shell("""echo "URL=$(.gitlab/ci/review-url.sh api-docs/index.html)">>dokka.env""")
		}

		artifacts {
			include("api-docs")
			dotenv("dokka.env")
		}

		environment {
			name("review/${Variable.Commit.Ref.slug}/api-docs")
			url($$"$URL")
			tier(Development)
		}

		interruptible(true)
	}

	if (Value.isTag) {
		job("pages", stage = deploy) {
			image("alpine", "latest")
			dependsOn(mkdocs, artifacts = true)
			dependsOn(dokka, artifacts = true)

			script {
				shell("mkdir -p public")
				shell("mv -T docs-website public")
				shell("mv api-docs public")
			}

			artifacts {
				include("public")
			}

			interruptible(false)
		}
	}

	// endregion
}.println()

println("""
	workflow:
	  rules:
	    - if: ${'$'}CI_PIPELINE_SOURCE == "parent_pipeline"
""".trimIndent())
