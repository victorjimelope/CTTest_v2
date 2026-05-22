# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -B -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S cttest && adduser -S cttest -G cttest

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
USER cttest

ENTRYPOINT ["java", "-jar", "app.jar"]
