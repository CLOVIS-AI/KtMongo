#!/usr/bin/env kotlin

// https://gitlab.com/opensavvy/automation/gitlab-ci.kt/-/releases
@file:DependsOn("dev.opensavvy.gitlab:gitlab-ci-kotlin-jvm:0.5.0")

import opensavvy.gitlab.ci.gitlabCi
import opensavvy.gitlab.ci.job
import opensavvy.gitlab.ci.script.shell
import opensavvy.gitlab.ci.stage

gitlabCi {
	val test by stage()

	val helloWorld by job(stage = test) {
		script {
			shell("echo 'Hello world'")
		}
	}
}.println()

println("""
	workflow:
	  rules:
	    - if: ${'$'}CI_PIPELINE_SOURCE == "parent_pipeline"
""".trimIndent())
