#!/usr/bin/env kotlin

// https://gitlab-ci-kts.opensavvy.dev/news/index.html
@file:DependsOn("dev.opensavvy.gitlab:gitlab-ci-kotlin-jvm:0.7.7")

import opensavvy.gitlab.ci.*
import opensavvy.gitlab.ci.Environment.EnvironmentTier.Development
import opensavvy.gitlab.ci.script.shell

/**
 * [OpenSavvy's CI container images](https://gitlab.com/opensavvy/automation/containers/-/releases)
 */
val ciContainers = "0.8.11"

/**
 * The URL of the website built by /docs/website.
 */
val siteUrl = "\$CI_PAGES_URL"

// ***

fun Job.opensavvyImage(name: String) =
	image("registry.gitlab.com/opensavvy/automation/containers/$name", ciContainers)

gitlabCi {
	val build by stage()
	val test by stage()
	val deploy by stage()

	// region Documentation

	val mkdocs by job(stage = build) {
		opensavvyImage("mkdocs")
		variable("GIT_DEPTH", "0")

		beforeScript {
			shell("cd docs/website")
			shell("ls")
			shell($$"""echo "repo_url: $CI_PROJECT_URL">>mkdocs.yml""")
			shell($$"""echo "repo_name: $CI_PROJECT_TITLE">>mkdocs.yml""")
			shell($$"""echo "site_url: $$siteUrl">>mkdocs.yml""")
			shell($$"""echo "Sitemap: $$siteUrl/sitemap.xml">>docs/robots.txt""")
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

	if (Value.isTag) {
		job("pages", stage = deploy) {
			image("alpine", "latest")
			dependsOn(mkdocs, artifacts = true)

			script {
				shell("mkdir -p public")
				shell("mv -T docs-website public")
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
