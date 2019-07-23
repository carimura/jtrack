#!/usr/bin/env bash

set -e

if [ -z "$1" ]; then
    echo "Please specify semantic version for Docker tagging (ie 1.0)"
    exit 1
fi

mvn clean package
docker build -t carimura/jimage:$1 .

