FROM gradle:5.1-jdk11 as build
WORKDIR /verify-service-provider
USER root

ARG VERIFY_USE_PUBLIC_BINARIES=false
ENV VERIFY_USE_PUBLIC_BINARIES $VERIFY_USE_PUBLIC_BINARIES

ENV GRADLE_USER_HOME ~/.gradle

COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src

RUN gradle --no-daemon installDist
ENTRYPOINT ["gradle", "--no-daemon"]

FROM openjdk:11-jre-slim
WORKDIR /verify-service-provider

COPY verify-service-provider.yml verify-service-provider.yml
COPY --from=build /verify-service-provider/build/install/verify-service-provider .

ENTRYPOINT ["sh", "-c"]
CMD ["bin/verify-service-provider", "server", "verify-service-provider.yml"]
