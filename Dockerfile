FROM openjdk:13

RUN curl -L https://raw.githubusercontent.com/denismakogon/java-opencv/master/apply_binaries.sh | /bin/bash

RUN yum update -y && yum install -y gtk2.x86_64

ADD lib/*.jar /usr/share/jimage/
ADD src/main/resources/* /usr/share/jimage/
ADD docker-entry.sh /docker-entry.sh
ADD oci_key.pem /usr/share/jimage/oci_key.pem

ADD target/jimage-1.0.jar  /usr/share/jimage/jimage.jar

ENTRYPOINT ["/bin/bash", "docker-entry.sh"]