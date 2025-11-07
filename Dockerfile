# 빌드 스테이지
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/gift-money-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
