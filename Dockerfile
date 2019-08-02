FROM oraclelinux:7-slim

RUN yum update -y && yum install -y gtk2.x86_64 && yum install -y tar
RUN curl -L https://raw.githubusercontent.com/denismakogon/oraclelinux-opencv/master/apply_binaries.sh | /bin/bash

ADD java-runtime /usr/share/jtrack/java-runtime/
ADD lib/*.jar /usr/share/jtrack/
ADD src/main/resources/* /usr/share/jtrack/
ADD target/classes /usr/share/jtrack/
ADD oci_key.pem /usr/share/jtrack/oci_key.pem

ENTRYPOINT ["/usr/share/jtrack/java-runtime/bin/java", "-cp", "/usr/share/jtrack/*:/usr/share/jtrack", "com.pinealpha.demos.jtrack.App"]