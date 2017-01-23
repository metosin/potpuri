#!/bin/bash

rev=$(git rev-parse HEAD)
remoteurl=$(git ls-remote --get-url origin)

if [[ ! -d doc ]]; then
    git clone --branch gh-pages ${remoteurl} doc
fi
(
cd doc
git pull
)

lein codox
cd doc
git add --all
git commit -m "Build docs from ${rev}."
git push origin gh-pages
