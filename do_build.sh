#!/usr/bin/env bash

set -xe

# get rid of lib if exist
rm -fr lib/
# get rid of target if exist
mvn clean
# copy dependencies only!
mvn package dependency:copy-dependencies -DincludeScope=runtime -DskipTests=true -Dmdep.prependGroupId=true -DoutputDirectory=lib --fail-never
# build our own JAR
mvn package
# do docker-build
docker build -t carimura/jimage:latest .
