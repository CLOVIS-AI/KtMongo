#!/usr/bin/env bash
# Script to verify that Dokka marker lines are adjacent to each other in mkdocs.yml

# Get line numbers of Dokka marker lines
START_LINE=$(grep -n "!!! EMBEDDED DOKKA START" docs/website/mkdocs.yml | cut -d':' -f1)
END_LINE=$(grep -n "!!! EMBEDDED DOKKA END" docs/website/mkdocs.yml | cut -d':' -f1)

# Check if both markers exist
if [ -z "$START_LINE" ] || [ -z "$END_LINE" ]; then
	echo "Error: The Dokka marker lines are missing from mkdocs.yml"
	exit 1
fi

# Check if markers are adjacent
if [ $((END_LINE - START_LINE)) -ne 1 ]; then
	echo "Error: The Dokka marker lines must be adjacent to each other"
	echo "START line: $START_LINE, END line: $END_LINE"
	echo "The Dokka table of content has been committed between the markers in the file mkdocs.yml. Please remove it before committing."
	exit 1
fi

echo "Dokka marker lines verification passed."
exit 0
