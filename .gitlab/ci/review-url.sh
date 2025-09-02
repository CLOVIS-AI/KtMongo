#!/usr/bin/env sh

################################################################################
#
#    Generates the review app URL for a given CI job.
#
#
#    Usage:
#      .gitlab/ci/review-url.sh ARTIFACT_PATH
#
################################################################################

# Yes, there are no predefined variables for this,
# so we use a ugly rewrite.
# https://gitlab.com/gitlab-org/gitlab/-/issues/450912

root_namespace=$(echo "$CI_PROJECT_NAMESPACE" | cut -d'/' -f1)
remaining_path=$(echo "$CI_PROJECT_NAMESPACE" | cut -d'/' -f2- -s)

echo "https://$root_namespace.$CI_PAGES_DOMAIN/-/$remaining_path/$CI_PROJECT_NAME/-/jobs/$CI_JOB_ID/artifacts/$1"
