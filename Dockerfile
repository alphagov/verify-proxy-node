# Base builder image

FROM gradle:jdk11 AS build
WORKDIR /app
USER root
ENV GRADLE_USER_HOME ~/.gradle

COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
COPY proxy-node-shared/ proxy-node-shared/

ARG component=proxy-node-gateway
COPY ${component}/src ${component}/src
COPY ${component}/build.gradle ${component}/build.gradle

RUN ./gradlew --no-daemon -p ${component} --parallel installDist -x test -x createPoms
ENTRYPOINT ["gradle", "--no-daemon"]

FROM openjdk:11-jre-slim
WORKDIR /app
USER root

ARG component
COPY --from=build /app/${component} /app/${component}

RUN apt-get update && apt-get install -y dumb-init

ENV CONFIG_FILE $component/build/install/$component/config.yml
ENV COMPONENT $component
ENTRYPOINT ["dumb-init", "--"]
CMD "/app/$COMPONENT/build/install/$COMPONENT/bin/$COMPONENT"
