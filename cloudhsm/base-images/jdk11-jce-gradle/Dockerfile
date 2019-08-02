# Base builder image to build apps that need the HSM

ARG JAVA_HOME=/opt/java/openjdk
ARG GRADLE_HOME=/opt/gradle
FROM gradle:jdk11 AS build

FROM amazonlinux:2.0.20190508

# Install Gradle and Java
ARG JAVA_HOME
ARG GRADLE_HOME

ENV JAVA_HOME $JAVA_HOME
ENV GRADLE_HOME $GRADLE_HOME

COPY --from=build $JAVA_HOME $JAVA_HOME
COPY --from=build $GRADLE_HOME $GRADLE_HOME

ENV PATH=$PATH:$GRADLE_HOME/bin
ENV PATH=$PATH:$JAVA_HOME/bin

ADD https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-2.0.0-3.el7.x86_64.rpm .
ADD https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-jce-2.0.0-3.el7.x86_64.rpm .

# Install AWS CloudHSM client and libs
RUN yum install -y ./cloudhsm-client-*.rpm \
 && rm ./cloudhsm-client-*.rpm

