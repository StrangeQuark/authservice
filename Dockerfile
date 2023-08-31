FROM eclipse-temurin:17-jdk-focal

WORKDIR /userservice

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

ENV PORT=8080
EXPOSE 8080

CMD ["./mvnw", "spring-boot:run"]