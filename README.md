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

### 1. Configure 
Create a .env file with GIPHY_TOKEN and SLACK_TOKEN

### 2. Include Dependencies

Each time you change dependencies: 
```
mvn package dependency:copy-dependencies -DincludeScope=runtime -DskipTests=true -Dmdep.prependGroupId=true -DoutputDirectory=lib --fail-neve
```

### 3. Compile

```
mvn clean package
```

### 4. Build Image
```
docker build -t <your_docker_name>/jimage:<version>
```

### 5. Profit
```
docker run -e "QUERY=happy" -e "NUM=3" --env-file=.env <your_docker_name>/jimage:<version>
```

 

## Next Up

1. Use loom to parallelize image pull and image push. (opencv underlying C is
   single threaded)
2. Post indicator of what's going on to Slack (while longer processing)
3. stroke isn't working to set strokesize.
