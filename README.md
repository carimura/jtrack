# jimage

## Overview

This program will: 

1. Get images from GIPHY API using passed in query
2. Try and detect faces on them, if found, draw a box
3. Post results to Slack

I think it's neat because `mvn package` will actually build the JAR into a
Java 12 Docker image (see Dockerfile) that can then be used anywhere. Portability yay!

You should think this is neat because some dependencies are super painful.
Like building OpenCV for your platform. 

You can also get images from Oracle object storage as well.

Example of face detection: 

<img src="https://raw.githubusercontent.com/carimura/jimage/master/result.gif" /> <br />

<img src="https://raw.githubusercontent.com/carimura/jimage/master/result.jpg" width=400/> <br />

## To Use

1. Create a .env file with GIPHY_TOKEN and SLACK_TOKEN
2. mvn clean build
3. `docker run -e "QUERY=faces" --env-file=.env <your_docker_name>/jimage:<version`

That's it. 

## Next Up

1. Figure out how to get more reliable images… giphy is unreliable for that jpg still frame image.
2. Reduce build time
3. Stretch goal: Use animated gifs. REbuild with detection boxes.