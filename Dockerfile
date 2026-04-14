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
COPY --from=build /workspace/target/freight-bot.jar ./freight-bot.jar
COPY .env .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/freight-bot.jar"]
