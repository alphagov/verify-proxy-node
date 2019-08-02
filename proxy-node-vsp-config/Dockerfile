FROM gradle:jdk11 as build
ENV GRADLE_USER_HOME ~/.gradle
WORKDIR /verify-service-provider

ARG RUN_TESTS=true
ARG VERIFY_USE_PUBLIC_BINARIES=false
ENV VERIFY_USE_PUBLIC_BINARIES $VERIFY_USE_PUBLIC_BINARIES

COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src

RUN gradle --parallel installDist
RUN if [ "$RUN_TESTS" = "true" ]; then gradle --parallel test ; fi

FROM openjdk:11-jre-slim
WORKDIR /verify-service-provider

COPY --from=build /verify-service-provider/build/install/verify-service-provider .
ADD https://raw.githubusercontent.com/alphagov/verify-proxy-node/master/proxy-node-vsp-config/verify-service-provider.yml verify-service-provider.yml

ENTRYPOINT ["sh", "-c"]
CMD ["bin/verify-service-provider", "server", "verify-service-provider.yml"]
