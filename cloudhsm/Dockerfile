# AWS CloudHSM client

FROM amazonlinux:2
WORKDIR /cloudhsm
ENV CLOUDHSM_CLIENT_VERSION=3.2.1-1.el7

# Install AWS CloudHSM client
RUN yum install -y wget \
 && wget --progress=bar:force https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-${CLOUDHSM_CLIENT_VERSION}.x86_64.rpm \
 && yum install -y ./cloudhsm-client-*.rpm

COPY init.sh .
RUN chmod +x init.sh

EXPOSE 1111
ENV HSM_IP=127.0.0.1
ENTRYPOINT ["/cloudhsm/init.sh"]
CMD ["/opt/cloudhsm/bin/cloudhsm_client", "/opt/cloudhsm/etc/cloudhsm_client.cfg"]
