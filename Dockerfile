FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies || true

COPY . .

RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

ENTRYPOINT ["java","-jar","app.jar"]
