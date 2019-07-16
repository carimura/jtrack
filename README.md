# jimage

## Overview

Very simple Java project that:

1. Gets file from Oracle cloud object storage (or local file)
2. Does facial recognition using OpenCV and boxes the faces
4. Puts the file back to object store

I think it's neat because `mvn package` will actually build the JAR into a
Java 12 Docker image (see Dockerfile) that can then be used anywhere. Portability yay!

You should think this is neat because some dependencies are super painful.
Like building OpenCV for your platform. 

Result should look like the following beautiful family:

<img src="https://raw.githubusercontent.com/carimura/jimage/master/result.jpg" width=400/> <br />

## To Use

1. You'll need your own oci_key.pem
2. mvn clean build
3. docker run <your_image>/<version (ie docker run carimura/jimage:0.5)


## Next Up

1. Pull images from a different source
2. Push results someplace more fun like Slack
3. Do something more interesting than simple face reco