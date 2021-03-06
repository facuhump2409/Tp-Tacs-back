ARG VERSION=8u151

FROM openjdk:11-jdk as BUILD

COPY . /src
WORKDIR /src
RUN ./gradlew build -x test --stacktrace

FROM openjdk:11-jre

COPY --from=BUILD /src/build/libs/wololo-0.0.1-SNAPSHOT.jar /bin/runner/run.jar
COPY --from=BUILD /src/src/main/resources/departamentos-argentina.json /bin/runner/src/main/resources/departamentos-argentina.json
COPY --from=BUILD /src/src/main/resources/game.properties /bin/runner/src/main/resources/game.properties

WORKDIR /bin/runner

CMD ["java","-jar","run.jar"]

# FROM openjdk:11
# ARG JAR_FILE=xd/*.jar
# COPY ${JAR_FILE} app.jar
# ENTRYPOINT ["java","-jar","/app.jar"]
