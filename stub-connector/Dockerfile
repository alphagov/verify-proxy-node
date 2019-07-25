FROM amazonlinux:2.0.20190508

# Install AWS CloudHSM client and libs
RUN yum install -y wget tar gzip \
 && wget --progress=bar:force https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-2.0.0-3.el7.x86_64.rpm \
 && yum install -y ./cloudhsm-client-*.rpm \
 && rm ./cloudhsm-client-*.rpm \
 && wget --progress=bar:force https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-jce-2.0.0-3.el7.x86_64.rpm \
 && yum install -y ./cloudhsm-client-jce-*.rpm \
 && rm ./cloudhsm-client-jce-*.rpm \
 && wget --progress=bar:force https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz \
 && mkdir -p /usr/lib/jvm \
 && tar -C /usr/lib/jvm -xf ./openjdk-11.0.2*.tar.gz \
 && rm ./openjdk-11.0.2*.tar.gz \
 && sed -i 's/UNIXSOCKET/TCPSOCKET/g' /opt/cloudhsm/data/application.cfg

ARG component=stub-connector
WORKDIR /app
ENV GRADLE_USER_HOME=/build/.gradle \
    LD_LIBRARY_PATH=/opt/cloudhsm/lib \
    HSM_PARTITION=PARTITION_1 \
    HSM_USER=user \
    HSM_PASSWORD=password \
    JAVA_HOME=/usr/lib/jvm/jdk-11.0.2 \
    COMPONENT=$component \
    CONFIG_FILE=/app/${component}/build/install/${component}/config.yml
COPY gradlew build.gradle settings.gradle ./
COPY gradle/ gradle/
COPY proxy-node-shared/ proxy-node-shared/
COPY ${component}/ ${component}/
RUN ./gradlew -Pcloudhsm --no-daemon -p ${component} installDist -x test

ENTRYPOINT "/app/$COMPONENT/build/install/$COMPONENT/bin/$COMPONENT"
