FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon :kafka-avro-genai-streaming-poc:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/kafka-avro-genai-streaming-poc/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
