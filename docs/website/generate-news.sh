#!/usr/bin/env bash

mkdir -p docs/news/posts/releases

for tag in $(git tag --list -n1 | cut -d' ' -f1); do
	echo "Found tag $tag"
	tag_file="docs/news/posts/releases/release-$tag.md"

	git for-each-ref \
		--format="---%ndate:%n  created: %(taggerdate:short)%nslug: v$tag%ntags: %n - Releases%n---%n%n# $tag • %(subject)%n%n%(contents:body)" \
		"refs/tags/$tag" \
		| sed 's/%n/\n/g' \
		| sed -r "s~\#([0-9]+)~[#\1]($CI_PROJECT_URL/-/issues/\1)~g" \
		| sed -r "s~([0-9a-f]{4,40})([,\)])~[\1]($CI_PROJECT_URL/-/commit/\1)\2~g" \
		| sed -r "s~!([0-9]+)~[!\1]($CI_PROJECT_URL/-/merge_requests/\1)~g" \
		>"$tag_file"

	echo -e "\n\n<!-- more -->" >>"$tag_file"
	echo -e "\n\n***\n\n[Browse the documentation for this version]($CI_PROJECT_URL/-/jobs/artifacts/$tag/browse/docs-website?job=mkdocs) • [View release in GitLab]($CI_PROJECT_URL/-/releases/$tag)" >>"$tag_file"
done
