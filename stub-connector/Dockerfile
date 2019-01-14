# Base builder image

FROM gradle:jdk11 AS build
WORKDIR /app
USER root
ENV GRADLE_USER_HOME ~/.gradle

COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY proxy-node-shared/ proxy-node-shared/

ARG component=stub-connector
COPY ${component}/src ${component}/src
COPY ${component}/build.gradle ${component}/build.gradle

RUN gradle --no-daemon --quiet -p ${component} --parallel install -x test
ENTRYPOINT ["gradle", "--no-daemon"]

FROM openjdk:11-jre-slim
WORKDIR /app
USER root

ARG component
COPY --from=build /app/${component}/build/install/${component} /app

ENV CONFIG_FILE config.yml
ENV COMPONENT $component
ENTRYPOINT ["/app/bin/stub-connector"]
CMD []
