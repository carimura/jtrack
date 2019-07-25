FROM openjdk:13

RUN curl -L https://raw.githubusercontent.com/denismakogon/java-opencv/master/apply_binaries.sh | /bin/bash

RUN yum update -y && yum install -y gtk2.x86_64

ADD lib/*.jar /usr/share/jimage/
ADD src/main/resources/* /usr/share/jimage/
ADD target/classes/* /usr/share/jimage/
ADD oci_key.pem /usr/share/jimage/oci_key.pem

ENTRYPOINT ["java", "-cp", "/usr/share/jimage/*:/usr/share/jimage", "com.pinealpha.demos.jimage.App"]