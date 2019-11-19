# Base builder image to build apps that need to talk to the HSM cient

FROM amazoncorretto:11

# Install AWS CloudHSM client and libs
ADD https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-2.0.0-3.el7.x86_64.rpm .
ADD https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-jce-2.0.0-3.el7.x86_64.rpm .

RUN yum install -y ./cloudhsm-client-*.rpm \
 && rm ./cloudhsm-client-*.rpm \
 && sed -i 's/UNIXSOCKET/TCPSOCKET/g' /opt/cloudhsm/data/application.cfg
