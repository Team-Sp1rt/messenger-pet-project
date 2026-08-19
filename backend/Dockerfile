FROM eclipse-temurin:26-jdk-alpine

WORKDIR /app

COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml .

RUN ./mvnw dependency:go-offline -B

COPY src src
COPY docs docs

RUN ./mvnw clean package -DskipTests -B

CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]