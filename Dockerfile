FROM gradle:8.9-jdk21-alpine AS build 
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle server:buildFatJar --no-daemon

FROM eclipse-temurin:21.0.1_12-jre-jammy
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/server/build/libs/*.jar /app/todo-list-kotlin.jar
ENTRYPOINT ["java", "-jar", "/app/todo-list-kotlin.jar"]