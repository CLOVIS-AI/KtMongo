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

package opensavvy.ktmongo.build

import org.gradle.api.Plugin
import org.gradle.api.Project

class KtMongoDslTemplatorPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		val templateTask = target.tasks.register("applyTemplate", ApplyTemplateTask::class.java) {
			group = "build"
			description = "Generate the sources of this module by templating the :dsl-template module. For more information, read dsl-template/README.md"

			sourceDir.set(project.rootProject.file("dsl-template/src/commonMain"))
			outputDir.set(project.file("src/commonMain"))
			projectRootDir.set(project.rootDir)
		}

		target.tasks.configureEach {
			if (name.contains("compileKotlin", ignoreCase = true) ||
				name.contains("sourcesJar", ignoreCase = true) ||
				name.contains("processResources", ignoreCase = true) ||
				name.contains("dokkaGenerate", ignoreCase = true) ||
				name.endsWith("KotlinMetadata", ignoreCase = true)) {
				dependsOn(templateTask)
			}
		}
	}
}
