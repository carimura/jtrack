FROM openjdk

ENTRYPOINT ["java", "-jar", "/usr/share/jimage/jimage.jar"]

RUN yum -y install ImageMagick
ADD src/main/resources/* /usr/share/jimage/
# get your own key yo
ADD oci_key.pem /usr/share/jimage/oci_key.pem

ADD target/jimage-0.1-jar-with-dependencies.jar     /usr/share/jimage/jimage.jar