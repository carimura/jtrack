#!/usr/bin/env bash

set -xe

/jdk/bin/java -cp $(echo /target/*.jar | tr ' ' ':') main.app.Main /original.gif
