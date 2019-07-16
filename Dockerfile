FROM denismakogon/java-11-opencv-debian:4.1.0-runtime

# required by javacv
RUN apt-get update && apt-get install --no-install-recommends -qy libgtk2.0
ADD src/main/resources/* /usr/share/jimage/
ADD oci_key.pem /usr/share/jimage/oci_key.pem
ADD target/jimage-0.3-jar-with-dependencies.jar     /usr/share/jimage/jimage.jar

ENTRYPOINT ["java", "-jar", "/usr/share/jimage/jimage.jar"]
