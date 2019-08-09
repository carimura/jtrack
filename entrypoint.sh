#!/usr/bin/env bash

set -xe

/jdk/bin/java -cp $(echo / target/org.bytedeco.*.jar | tr ' ' ':') app.main.entrypoint.App
