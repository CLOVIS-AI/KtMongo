#!/usr/bin/env sh

# Upgrades all Gradle dependencies
#
# Usage:
#   ./gradle/upgrade-all.sh
#   (from the project directory)

caupain() {
	docker run \
		--pull=always \
		--rm \
		--user="$(id -u):$(id -g)" \
		--volume="./gradle:/work" \
		--workdir="/work" \
		registry.gitlab.com/opensavvy/automation/containers/caupain:latest \
		$*
}

caupain -i common.versions.toml --in-place --policy=always --no-cache
caupain -i libs.versions.toml --in-place --policy=always --no-cache
