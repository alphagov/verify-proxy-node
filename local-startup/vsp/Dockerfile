FROM gradle:6.4.1-jdk11 AS build
WORKDIR /vsp

ARG USE_LOCAL_BUILD=true
ARG VERIFY_USE_PUBLIC_BINARIES=false
ENV VERIFY_USE_PUBLIC_BINARIES $VERIFY_USE_PUBLIC_BINARIES

COPY src/ src/
COPY build.gradle settings.gradle ./

RUN mkdir -p build/install/verify-service-provider
RUN if [ "$USE_LOCAL_BUILD" = "false" ]; then \
    gradle --console plain --no-daemon --parallel installDist ; fi

COPY verify-service-provider.yml build/install/verify-service-provider/

FROM openjdk:11-jre-slim
WORKDIR /app
ARG USE_LOCAL_BUILD=true
ARG LOCAL_HUB=false

ARG TINI_VERSION=v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

COPY --from=build /vsp/build/install/verify-service-provider build/

RUN if [ "$USE_LOCAL_BUILD" = "true" ]; then source="local"; else source="build"; fi; \
    if [ "$LOCAL_HUB" = "true" ]; then config="verify-service-provider-local-hub.yml"; else config="$source/verify-service-provider.yml"; fi; \
    ln -s $source/bin/verify-service-provider verify-service-provider; \
    ln -s $config config.yml

ENTRYPOINT ["/tini", "--", "./verify-service-provider"]
CMD ["server", "config.yml"]