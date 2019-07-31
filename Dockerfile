# Base builder image

FROM gradle:jdk11 AS build
ENV GRADLE_USER_HOME ~/.gradle
WORKDIR /app

COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
COPY proxy-node-shared/ proxy-node-shared/
COPY proxy-node-test/ proxy-node-test/

ARG RUN_TESTS=true
ARG VERIFY_USE_PUBLIC_BINARIES=false
ENV VERIFY_USE_PUBLIC_BINARIES $VERIFY_USE_PUBLIC_BINARIES

ARG component
COPY ${component}/src ${component}/src
COPY ${component}/build.gradle ${component}/build.gradle

RUN gradle --parallel -p ${component} installDist
RUN if [ "$RUN_TESTS" = "true" ]; then \
      gradle --parallel \
        -x createPoms \
        -x :proxy-node-shared:jar \
        -x :${component}:jar \
        :proxy-node-shared:test \
        :${component}:test ; fi

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
