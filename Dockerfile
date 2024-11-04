FROM openjdk:17-jdk-slim
CMD ["./gradlew", "clean", "build"]
WORKDIR /tmp
COPY build/libs/app.jar /tmp/app.jar
COPY coin-config/application-prod.yml /app/config/application-prod.yml
COPY coin-config/logback.xml /app/config/logback.xml
ENV SPRING_CONFIG_LOCATION=/app/config/application.yml
ENV LOGGING_CONFIG=/app/config/logback.xml
ENTRYPOINT ["java","-jar","/tmp/app.jar"]