#!/usr/bin/env bash

set -e

java -cp $(echo /usr/share/jimage/*.jar | tr ' ' ':') com.pinealpha.demos.jimage.App
