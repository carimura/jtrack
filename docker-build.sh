#!/usr/bin/env bash

set -xe

docker build -t $1:$2 .
