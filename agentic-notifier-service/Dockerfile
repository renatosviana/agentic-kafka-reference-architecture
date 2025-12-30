FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon :agentic-notifier-service:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/agentic-notifier-service/build/libs/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java","-jar","app.jar"]
