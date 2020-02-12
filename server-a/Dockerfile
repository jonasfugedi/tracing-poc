FROM openjdk:15-jdk-alpine

ADD target/lib /app/lib
ARG JAR_FILE
ADD target/${JAR_FILE} /app/service.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/service.jar"]
