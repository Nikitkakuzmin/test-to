FROM openjdk:17-oracle
LABEL maintainer="nik"
COPY build/libs/test-to-0.0.1-SNAPSHOT.jar sensors.jar
ENTRYPOINT ["java", "-jar", "sensors.jar"]