FROM gradle:6.4.1-jdk11 AS build
WORKDIR /build

ARG component
ARG USE_LOCAL_BUILD=true
ARG VERIFY_USE_PUBLIC_BINARIES=false
ENV VERIFY_USE_PUBLIC_BINARIES $VERIFY_USE_PUBLIC_BINARIES

COPY ${component}/ ${component}/
COPY build.gradle settings.gradle ./
COPY proxy-node-shared/ proxy-node-shared/

RUN mkdir -p /build/${component}/build/install/${component}
RUN if [ "$USE_LOCAL_BUILD" = "false" ]; then \
    gradle --console plain --no-daemon --parallel :${component}:installDist ; fi

FROM openjdk:11-jre-slim
WORKDIR /app
ARG USE_LOCAL_BUILD=true

ARG TINI_VERSION=v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

ARG component
COPY --from=build /build/${component}/build/install/${component} build/

RUN if [ "$USE_LOCAL_BUILD" = "true" ]; then source="local"; else source="build"; fi; \
    ln -s $source/config.yml config.yml; ln -s $source/bin/$component $component

ENV CONFIG_FILE config.yml
ENV COMPONENT $component

ENTRYPOINT ["/tini", "--"]
CMD "./$COMPONENT"
