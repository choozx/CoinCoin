plugins {
    java
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.jh"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket:3.4.2")
    implementation("org.projectlombok:lombok:1.18.22")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
    implementation("com.slack.api:bolt:1.42.0")
    implementation("com.slack.api:bolt-servlet:1.42.0")
    implementation("com.slack.api:bolt-jetty:1.42.0")
    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("org.jboss.logging:jboss-logging:3.4.1.Final")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
