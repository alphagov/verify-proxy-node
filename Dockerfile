# Base builder image

FROM gradle:jdk11 AS build
WORKDIR /app
ENV GRADLE_USER_HOME ~/.gradle

COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
COPY proxy-node-shared/ proxy-node-shared/

ARG VERIFY_USE_PUBLIC_BINARIES=false
ENV VERIFY_USE_PUBLIC_BINARIES $VERIFY_USE_PUBLIC_BINARIES

ARG component
COPY ${component}/src ${component}/src
COPY ${component}/build.gradle ${component}/build.gradle

RUN gradle --no-daemon -p ${component} --parallel installDist -x test -x createPoms
ENTRYPOINT ["gradle", "--no-daemon"]

FROM openjdk:11-jre-slim
WORKDIR /app

ARG TINI_VERSION=v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

ARG component
COPY --from=build /app/${component}/build/install/${component} .

ENV CONFIG_FILE config.yml
ENV COMPONENT $component

ENTRYPOINT ["/tini", "--"]
CMD "bin/$COMPONENT"

