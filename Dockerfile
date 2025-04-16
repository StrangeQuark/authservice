# Stage 1: Build the application
FROM eclipse-temurin:21-alpine AS builder

WORKDIR /authservice

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && sed -i 's/\r$//' mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Create minimal runtime image
FROM eclipse-temurin:21-alpine

WORKDIR /authservice

COPY --from=builder /authservice/target/*.jar authservice.jar

ENV JAVA_OPTS=""

EXPOSE 6001

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar authservice.jar"]
