#!/usr/bin/env bash

set -xe

lib_path="lib64"

mkdir -p /usr/local/include/opencv4
curl -L https://github.com/denismakogon/java-opencv/raw/master/release/java-13/include_opencv4.tar.gz  | \
    tar xvz -C /

mkdir -p /usr/local/${lib_path}
curl -L https://github.com/denismakogon/java-opencv/raw/master/release/java-13/${lib_path}.tar.gz | \
    tar xvz -C /

