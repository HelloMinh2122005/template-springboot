# Build stage
FROM maven:3.8-eclipse-temurin-17 AS build
COPY src /home/app/src

COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests=true

# Package stage
FROM eclipse-temurin:17-jre-focal
COPY --from=build /home/app/target/template-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]
CMD env && java -jar /usr/local/lib/app.jar

