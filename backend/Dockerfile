FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY ./src src
RUN mvn clean install -DskipTests
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted


FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Copy only necessary layers from the extracted JAR
COPY --from=builder app/target/extracted/dependencies/ ./
COPY --from=builder app/target/extracted/spring-boot-loader/ ./
COPY --from=builder app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder app/target/extracted/application/ ./

# Inject description from an external file at build time
ARG IMAGE_DESCRIPTION=""
LABEL org.opencontainers.image.description="$IMAGE_DESCRIPTION"
LABEL org.opencontainers.image.source="https://github.com/jordybronowicki37/befit-bot"

ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]
