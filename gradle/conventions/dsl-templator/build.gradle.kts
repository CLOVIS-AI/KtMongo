/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

plugins {
	`kotlin-dsl`
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.plugin)
	antlr
}

tasks.generateGrammarSource {
	arguments = arguments + listOf("-package", "opensavvy.ktmongo.build.kotlin", "-visitor")
}

gradlePlugin {
	plugins {
		create("antlr-kotlin") {
			id = "dev.opensavvy.ktmongo.build.templator"
			implementationClass = "opensavvy.ktmongo.build.KtMongoDslTemplatorPlugin"
		}
	}
}
