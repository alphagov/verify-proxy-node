# Base builder image

FROM gradle:5.5.1-jdk11 AS build
WORKDIR /app

ARG component
ARG USE_LOCAL_BUILD=true
ARG VERIFY_USE_PUBLIC_BINARIES=false
ENV VERIFY_USE_PUBLIC_BINARIES $VERIFY_USE_PUBLIC_BINARIES

COPY ${component}/ ${component}/
COPY build.gradle settings.gradle ./
COPY proxy-node-shared/ proxy-node-shared/

RUN if [ "$USE_LOCAL_BUILD" = "false" ] || ! [ -x ${component}/build/install/${component} ]; then \
    gradle --console plain --no-daemon --parallel :${component}:installDist ; \
    else echo "Using local build" ; fi

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
