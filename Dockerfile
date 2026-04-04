FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY . .
RUN ./mvnw package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/target/pulsedesk-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]