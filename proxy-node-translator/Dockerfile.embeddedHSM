#
#   Docker Image for Translator Component with local softHSM.
#   Translator depends on softHSM and OpenSC installations in Test environments.
#
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

# -------------------- Runtime Image --------------------
FROM openjdk:11-jre-slim

# ----------------------------------------------------------
# Install softHSM and dependencies for OpenSC
RUN apt-get update && apt-get install -y softhsm pcscd libccid libpcsclite-dev libssl-dev libreadline-dev autoconf automake build-essential docbook-xsl xsltproc libtool pkg-config wget

# Download and build OpenSC
ENV OPEN_SC_VERSION=0.19.0
RUN wget https://github.com/OpenSC/OpenSC/releases/download/${OPEN_SC_VERSION}/opensc-${OPEN_SC_VERSION}.tar.gz && \
    tar xfvz opensc-${OPEN_SC_VERSION}.tar.gz && \
    cd opensc-${OPEN_SC_VERSION} && \
    ./bootstrap && ./configure --prefix=/usr --sysconfdir=/etc/opensc && \
    make && make install && \
    cd .. && rm -rf opensc* && \
    rm -rf /var/lib/apt/lists/*

# ----------------------------------------------------------

# Copy java app from build to runtime image
WORKDIR /app
USER root

ARG component=proxy-node-translator
COPY --from=build /app/${component}/build/install/${component} /app
COPY --from=build /app/${component}/src/dist/init.sh /bin/init.sh

ENV CONFIG_FILE config.yml
ENV COMPONENT $component
ENTRYPOINT ["/bin/init.sh"]
CMD []
