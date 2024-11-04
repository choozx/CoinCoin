FROM openjdk:17-jdk-slim
CMD ["./gradlew", "clean", "build"]
WORKDIR /app
COPY build/libs/app.jar /app/app.jar
COPY coin-config/application-prod.yml /app/config/application-prod.yml
COPY coin-config/logback-prod.xml /app/config/logback-prod.xml
ENV SPRING_CONFIG_LOCATION=/app/config/application-prod.yml
ENV LOGGING_CONFIG=/app/config/logback-prod.xml
ENTRYPOINT ["java","-jar","/tmp/app.jar"]