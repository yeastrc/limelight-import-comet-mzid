FROM amazoncorretto:11-alpine-jdk

COPY build/libs/cometMzid2LimelightXML.jar  /usr/local/bin/cometMzid2LimelightXML.jar

ENTRYPOINT ["java", "-jar", "/usr/local/bin/cometMzid2LimelightXML.jar"]