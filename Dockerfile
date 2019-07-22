FROM openjdk:13
#FROM denismakogon/java-11-opencv-debian:4.1.0-runtime

RUN curl -L https://raw.githubusercontent.com/denismakogon/java-opencv/master/apply_binaries.sh | /bin/bash

RUN yum update -y && yum install -y gtk2.x86_64

#RUN apt-get update && apt-get install --no-install-recommends -qy libgtk2.0

ADD src/main/resources/* /usr/share/jimage/
ADD oci_key.pem /usr/share/jimage/oci_key.pem
ADD target/jimage-0.7-jar-with-dependencies.jar  /usr/share/jimage/jimage.jar

ENTRYPOINT ["java", "-jar", "/usr/share/jimage/jimage.jar"]