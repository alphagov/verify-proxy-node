#!/usr/bin/env bash

pull_request_data() {
  cat << EOF
{
  "title": "Update project dependencies file pom.xml",
  "body": "Changes to dependencies in commit $(git rev-parse HEAD)"
  "head": "$1",
  "base": "master"
}
EOF
}

open_pull_request() {
    curl -u ida-codacy-bot:$GITHUB_PAT -i \
    -H "Content-Type:application/json" \
    -X POST --data "$(pull_request_data $1)" "https://api.github.com/repos/alphagov/verify-proxy-node/pulls"
}

if [ "$TRAVIS_BRANCH" = "master" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
  if ! git diff -s --exit-code dependencies/*/pom.xml; then
    branch_name=bau-update-project-pom-$(date +%Y%m%d-%H%M%S)
    git checkout -b $branch_name
    git add dependencies/*/pom.xml
    git commit -m "Update project dependencies POM files"
    git push
    open_pull_request $branch_name
  fi
fi
