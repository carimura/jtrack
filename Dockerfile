FROM openjdk:12

ENTRYPOINT ["java", "-jar", "/usr/share/jimage/jimage.jar"]

ADD src/main/resources/* /usr/share/jimage/
# get your own key yo
ADD oci_key.pem /usr/share/jimage/oci_key.pem
ADD target/jimage-0.5-jar-with-dependencies.jar     /usr/share/jimage/jimage.jar

