FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY Ui.java .
RUN javac -encoding UTF-8 Ui.java && jar --create --file app.jar --main-class Ui Ui.class

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/app.jar .
USER 10001
ENTRYPOINT ["java", "-jar", "app.jar"]
