# Base builder image

FROM gradle:jdk8 AS build
WORKDIR /app
USER root
ENV GRADLE_USER_HOME ~/.gradle

COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY proxy-node-shared/src proxy-node-shared/src
COPY proxy-node-shared/build.gradle proxy-node-shared/build.gradle

ARG component
COPY ${component}/src ${component}/src
COPY ${component}/build.gradle ${component}/build.gradle

RUN gradle --no-daemon --quiet -p ${component} install

FROM openjdk:8-jre-slim
WORKDIR /app
USER root

ARG component
COPY --from=build /app/${component}/build/install/${component} /app

ENV CONFIG_FILE config.yml
ENV COMPONENT $component
CMD "bin/$COMPONENT"
