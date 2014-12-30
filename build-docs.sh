#!/bin/bash

if [[ $TRAVIS_PULL_REQUEST == "true" ]]; then
    echo "PR, not building docs."
    exit 0
fi

deploy_url="https://${GH_TOKEN}@github.com/metosin/potpuri.git"
rev=$(git rev-parse HEAD)

git config user.name "$GIT_NAME"
git config user.email "$GIT_EMAIL"

git clone --branch gh-pages $deploy_url doc
lein doc
cd doc
git add --all
git commit -m "Build docs from ${rev}."
git push -q $deploy_url gh-pages
