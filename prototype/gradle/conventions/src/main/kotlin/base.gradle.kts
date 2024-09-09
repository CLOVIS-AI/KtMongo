package conventions

val appVersion: String? by project

version = appVersion ?: "DEV"
group = "fr.4sh.ktmongo"

tasks.withType<Test> {
	useJUnitPlatform()
}
