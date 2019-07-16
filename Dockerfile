FROM openjdk:9

ENTRYPOINT ["java", "-jar", "/usr/share/jimage/jimage.jar"]

RUN apt-get -y update
RUN apt-get install -y imagemagick

ADD src/main/resources/* /usr/share/jimage/
# get your own key yo
ADD oci_key.pem /usr/share/jimage/oci_key.pem
ADD target/jimage-0.3-jar-with-dependencies.jar     /usr/share/jimage/jimage.jar

