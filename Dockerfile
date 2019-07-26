FROM openjdk:13
#FROM oraclelinux:latest

RUN curl -L https://raw.githubusercontent.com/denismakogon/oraclelinux-opencv/master/apply_binaries.sh | /bin/bash

RUN yum update -y && yum install -y gtk2.x86_64

#ADD java-runtime /usr/share/jimage/java-runtime/
ADD lib/*.jar /usr/share/jimage/
ADD src/main/resources/* /usr/share/jimage/
ADD target/classes /usr/share/jimage/
ADD oci_key.pem /usr/share/jimage/oci_key.pem

ENTRYPOINT ["/usr/share/jimage/java-runtime/bin/java", "-cp", "/usr/share/jimage/*:/usr/share/jimage", "com.pinealpha.demos.jimage.App"]