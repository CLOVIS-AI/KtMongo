/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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
	alias(opensavvyConventions.plugins.base)
	id("dev.opensavvy.dokka-mkdocs")
}

dependencies {
	// List the 'library' projects
	dokka(projects.annotations)
	dokka(projects.bson)
	dokka(projects.bsonOfficial)
	dokka(projects.bsonMultiplatform)
	dokka(projects.bsonTests)
	dokka(projects.dsl)
	dokka(projects.driverSharedOfficial)
	dokka(projects.driverSync)
	dokka(projects.driverSyncJava)
	dokka(projects.driverCoroutines)
}
