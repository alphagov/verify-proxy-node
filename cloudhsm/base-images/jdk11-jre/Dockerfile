# Base builder image to run apps that need the HSM

FROM eidas/cloudhsm-jce-gradle AS build
WORKDIR /app

COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
COPY proxy-node-shared/ proxy-node-shared/

ARG VERIFY_USE_PUBLIC_BINARIES=false
ENV VERIFY_USE_PUBLIC_BINARIES $VERIFY_USE_PUBLIC_BINARIES

ARG component
COPY ${component}/ ${component}/
RUN gradle -Pcloudhsm --no-daemon -p ${component} installDist -x test
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

