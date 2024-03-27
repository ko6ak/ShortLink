FROM openjdk:latest as build
WORKDIR /app
COPY target/*.war /app/app.war
CMD ["java", "-jar", "app.war"]