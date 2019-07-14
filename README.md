# jimage

Very simple Java project that:

1. Gets file from Oracle cloud object storage
2. Rotates using ImageMagick (by way of im4java)
3. or... Does facial recognition using OpenCV and boxes the faces
4. Puts the file back to object store

I think it's neat because `mvn package` will actually build the JAR into a
Java 12 Docker image (see Dockerfile) where ImageMagick is patiently waiting.

You should think this is neat because some dependencies are super painful. 
Like, who tarballs and `makes` shit still? Also that pom file is kind of magical.

The Java code probably sucks. I have a lot to catch up on.

Don't judge me.

Update: actually using openjdk:9 until I figure out why the opencv maven repo 
doesn't like 10+. 



