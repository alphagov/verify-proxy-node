# Base builder image to build apps that need to talk to the HSM cient

FROM amazoncorretto:11
ENV LANG C.UTF-8
ENV CLOUDHSM_CLIENT_VERSION=3.2.1-1.el7

# Install AWS CloudHSM client and libs
ADD https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-${CLOUDHSM_CLIENT_VERSION}.x86_64.rpm .
ADD https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-jce-${CLOUDHSM_CLIENT_VERSION}.x86_64.rpm .

RUN yum install -y ./cloudhsm-client-*.rpm \
 && rm ./cloudhsm-client-*.rpm \
 && sed -i 's/UNIXSOCKET/TCPSOCKET/g' /opt/cloudhsm/data/application.cfg

RUN rm /opt/cloudhsm/java/cloudhsm-test*.jar && \
    rm /opt/cloudhsm/java/hamcrest*.jar && \
    rm /opt/cloudhsm/java/junit*.jar
