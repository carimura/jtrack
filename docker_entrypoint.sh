#!/usr/bin/env bash

set -xe

java -cp $(echo ${PWD}/*.jar | tr ' ' ':') com.pinealpha.demos.jimage.App
