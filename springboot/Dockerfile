FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/target/shadownet-nexus-1.0.0.jar /app/app.jar

EXPOSE 3001

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-3001} -Dserver.address=0.0.0.0 -jar /app/app.jar"]
