#!/usr/bin/env bash

set -xe

os_type="none"
os_arch=$(uname -m)

case "$OSTYPE" in
  darwin*)  os_type="macosx" ;;
  linux*)   os_type="linux" ;;
  *)        os_type="unknown: $OSTYPE" ;;
esac

mvn clean
mvn clean package dependency:copy-dependencies \
    -DincludeScope=runtime \
    -DskipTests=true \
    -Dmdep.prependGroupId=true \
    -DoutputDirectory=../target \
    -DskipDockerBuild=true \
    --fail-never \
    -Dplatform.id=${os_type}-${os_arch}
mvn package \
    -Dplatform.id=${os_type}-${os_arch} \
    -DskipDockerBuild=true
