# AWS CloudHSM client

FROM amazonlinux:2
WORKDIR /cloudhsm

# Install AWS CloudHSM client
RUN yum install -y wget \
 && wget --progress=bar:force https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-3.0.0-2.el7.x86_64.rpm \
 && yum install -y ./cloudhsm-client-*.rpm

COPY init.sh .
RUN chmod +x init.sh

EXPOSE 1111
ENV HSM_IP=127.0.0.1
ENTRYPOINT ["/cloudhsm/init.sh"]
CMD ["/opt/cloudhsm/bin/cloudhsm_client", "/opt/cloudhsm/etc/cloudhsm_client.cfg"]
