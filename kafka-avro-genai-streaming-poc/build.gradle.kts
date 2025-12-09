plugins {
    id("java")
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.8.0"
}

group = "com.viana.poc"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams:3.7.0")

    implementation("io.confluent:kafka-avro-serializer:7.5.0")
    implementation("io.confluent:kafka-streams-avro-serde:7.5.0")
    implementation("org.apache.avro:avro:1.11.3")

    implementation("org.springframework.boot:spring-boot-starter-webflux")


    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql:42.7.4")


    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
}


avro {
    stringType.set("String")
}

springBoot {
    mainClass.set("com.viana.poc.KafkaAvroPocApplication")
}


tasks.withType<Test> {
    useJUnitPlatform()
}
