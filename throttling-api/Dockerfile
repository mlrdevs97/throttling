# STAGE 1: Build the .war file from source
FROM maven:3.8.5-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# STAGE 2: Create the image with Tomcat
FROM tomcat:10.1-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/ThrottlingAPI.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080