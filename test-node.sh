#!/bin/bash

lein do cljx once, cljsbuild once
node target/generated/js/tests.js
