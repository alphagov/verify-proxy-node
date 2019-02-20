from amazonlinux:2.0.20190212
workdir /cloudhsm

# Install AWS CloudHSM client
run yum install -y wget \
 && wget https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-latest.el7.x86_64.rpm \
 && yum install -y ./cloudhsm-client-latest.*.rpm

copy init.sh .
run chmod +x init.sh

expose 1111
env HSM_IP=127.0.0.1
entrypoint ["/cloudhsm/init.sh"]
cmd ["/opt/cloudhsm/bin/cloudhsm_client", "/opt/cloudhsm/etc/cloudhsm_client.cfg"]
