#!/usr/bin/env bash

# removing snapshot marker:
mvn -q build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.incrementalVersion} versions:commit

# building
mvn -q -DskipTests clean package

ls -lha target/*

# SAM packaging
sam package --s3-bucket scanii-assets --s3-prefix sam/guarana  --template-file template.yml --output-template-file guarana.yaml || exit 1
