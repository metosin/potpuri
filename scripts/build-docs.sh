#!/bin/bash

set -euo pipefail

rev=$(git rev-parse HEAD)
remoteurl=$(git ls-remote --get-url origin)
repodir=doc

git fetch
if [[ -z $(git branch -r --list origin/gh-pages) ]]; then
    (
    mkdir "$repodir"
    cd "$repodir"
    git init
    git remote add origin "${remoteurl}"
    git checkout -b gh-pages
    git commit --allow-empty -m "Init"
    git push -u origin gh-pages
    )
elif [[ ! -d gh-pages ]]; then
    git clone --branch gh-pages "${remoteurl}" "$repodir"
else
    (
    cd "$repodir"
    git pull
    )
fi

lein codox
cd "$repodir"
git add --all
git commit -m "Build docs from ${rev}."
git push origin gh-pages
