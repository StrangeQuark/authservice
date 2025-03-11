FROM eclipse-temurin:21-alpine

WORKDIR /authservice

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN sed -i 's/\r$//' mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src

ENV PORT=6001
EXPOSE 6001

CMD ["./mvnw", "spring-boot:run"]