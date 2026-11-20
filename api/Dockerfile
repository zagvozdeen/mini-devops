FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY Api.java .
RUN javac -encoding UTF-8 Api.java && jar --create --file app.jar --main-class Api Api.class

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/app.jar .
ADD --chmod=0444 https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.8/postgresql-42.7.8.jar jdbc.jar
USER 10001
ENTRYPOINT ["java", "-cp", "app.jar:jdbc.jar", "Api"]
