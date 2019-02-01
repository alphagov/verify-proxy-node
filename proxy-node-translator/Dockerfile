FROM openjdk:11-jre-slim AS libs
WORKDIR /app
ARG version=2.5.0

RUN apt-get update && \
    apt-get install -y \
    opensc build-essential wget libsqlite3-dev libssl-dev \
    cmake libseccomp-dev

RUN wget https://github.com/SUNET/pkcs11-proxy/archive/master.zip && \
    unzip master.zip && \
    cd pkcs11-proxy-master && \
    cmake . && make && make install

FROM gradle:jdk11 AS build
WORKDIR /app
USER root
ENV GRADLE_USER_HOME ~/.gradle

COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY proxy-node-shared/ proxy-node-shared/

ARG component=proxy-node-translator
COPY ${component}/src ${component}/src
COPY ${component}/build.gradle ${component}/build.gradle

RUN gradle --no-daemon --quiet -p ${component} --parallel install -x test
ENTRYPOINT ["gradle", "--no-daemon"]

FROM openjdk:11-jre-slim
WORKDIR /app
USER root

COPY --from=libs /usr/local/lib/libpkcs11-proxy* /usr/local/lib/
COPY --from=libs /usr/local/bin/pkcs11-daemon /usr/local/bin/

ARG component=proxy-node-translator
COPY --from=build /app/${component}/build/install/${component} /app

RUN apt-get update && apt-get install -y dumb-init

ENV CONFIG_FILE config.yml
ENV COMPONENT $component
ENV SOFT_HSM_LIB_PATH /usr/local/lib/libpkcs11-proxy.so
ENV KEY_RETRIEVER_SERVICE_NAME softhsm
ENTRYPOINT ["dumb-init", "--"]
CMD "/app/bin/$COMPONENT"
