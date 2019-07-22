FROM openjdk:13


RUN curl -L https://raw.githubusercontent.com/denismakogon/java-opencv/master/apply_binaries.sh | /bin/bash

RUN yum update -y && yum install -y gtk2.x86_64
# meant to be statics
ADD lib/*.jar /usr/share/jimage/
ADD src/main/resources/* /usr/share/jimage/
ADD oci_key.pem /usr/share/jimage/oci_key.pem
ADD docker_entrypoint.sh /docker_entrypoint.sh

WORKDIR /usr/share/jimage/

ADD target/jimage-0.7.jar  /usr/share/jimage/jimage.jar

ENTRYPOINT ["/bin/bash", "/docker_entrypoint.sh"]
