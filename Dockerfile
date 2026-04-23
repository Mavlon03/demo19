FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw -q -N io.takari:maven:wrapper
COPY src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/*.jar ./freight-bot.jar
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --retries=5 CMD ["bash", "-lc", "exec 3<>/dev/tcp/127.0.0.1/8080 && printf 'GET /api/health HTTP/1.1\r\nHost: localhost\r\n\r\n' >&3 && grep '200' <&3"]
ENTRYPOINT ["java", "-jar", "/app/freight-bot.jar"]
