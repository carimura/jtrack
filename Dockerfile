FROM oraclelinux:7-slim as maven-stage

ENV JAVA_HOME=/jdk-14
RUN yum update -y && \
    yum install -y tar gzip && \
    curl -L https://download.java.net/java/early_access/loom/2/openjdk-14-loom+2-4_linux-x64_bin.tar.gz | tar xvz -C / && \
    curl -L http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -o /etc/yum.repos.d/epel-apache-maven.repo && \
    yum update -y && yum install -y apache-maven

ADD pom.xml /project/

ADD api.gif.com/ /project/api.gif.com
ADD api.facedetect.com/ /project/api.facedetect.com
ADD api.oci.oracle /project/api.oci.oracle
ADD api.services.com /project/api.services.com
ADD app.main.entrypoint /project/app.main.entrypoint

ADD build.sh /project/

WORKDIR /project

RUN mvn clean
RUN mvn clean package dependency:copy-dependencies \
        -DincludeScope=runtime \
        -DskipTests=true \
        -Dmdep.prependGroupId=true \
        -DoutputDirectory=../target \
        -DskipDockerBuild=true \
        --fail-never \
        -Dplatform.id=linux-x86_64
RUN mvn package -DskipDockerBuild=true -Dplatform.id=linux-x86_64

RUN yum clean all -y

FROM oraclelinux:7-slim

COPY --from=maven-stage /project/target/org.bytedeco* /target/
COPY --from=maven-stage /project/app.main.entrypoint/target/maven-jlink /jdk

RUN yum update -y && yum install -y gtk2

ADD entrypoint.sh /entrypoint.sh
ADD app.main.entrypoint/src/main/resources/* /usr/share/jtrack/
ADD oci_key.pem /usr/share/jtrack/oci_key.pem

ENV JAVA_HOME=/jdk

ENTRYPOINT ["/bin/bash", "entrypoint.sh"]
