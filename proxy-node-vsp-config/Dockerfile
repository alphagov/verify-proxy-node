FROM gradle:jdk11 as build
WORKDIR /verify-service-provider

ARG VERIFY_USE_PUBLIC_BINARIES=false
ENV VERIFY_USE_PUBLIC_BINARIES $VERIFY_USE_PUBLIC_BINARIES

ENV GRADLE_USER_HOME ~/.gradle

COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src

RUN gradle --no-daemon --parallel installDist -x test
ENTRYPOINT ["gradle", "--no-daemon"]

FROM openjdk:11-jre-slim
WORKDIR /verify-service-provider

COPY --from=build /verify-service-provider/build/install/verify-service-provider .
ADD https://raw.githubusercontent.com/alphagov/verify-proxy-node/master/proxy-node-vsp-config/verify-service-provider.yml verify-service-provider.yml

ENTRYPOINT ["sh", "-c"]
CMD ["bin/verify-service-provider", "server", "verify-service-provider.yml"]
