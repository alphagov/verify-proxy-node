# build
from gradle:jdk11 AS build
env WORKDIR=/mdgen
workdir $WORKDIR
user root

env GRADLE_USER_HOME $WORKDIR/.gradle
copy build.gradle build.gradle
copy settings.gradle settings.gradle
copy src/ src/
copy libs/ libs/

run gradle --no-daemon installDist -x test

# runtime
from amazonlinux:2.0.20190212

# Install AWS CloudHSM client and libs
run yum install -y wget

run wget https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-latest.el7.x86_64.rpm \
 && yum install -y ./cloudhsm-client-latest.*.rpm \
 && rm ./cloudhsm-client-latest.*.rpm

run wget https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-jce-latest.el7.x86_64.rpm \
 && yum install -y ./cloudhsm-client-jce-latest.*.rpm \
 && rm ./cloudhsm-client-jce-latest.*.rpm

# Install Java 11
run yum install -y tar gzip \
 && wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz \
 && mkdir -p /usr/lib/jvm \
 && tar -C /usr/lib/jvm -xf ./openjdk-11.0.2*.tar.gz \
 && rm ./openjdk-11.0.2*.tar.gz

copy --from=build /mdgen/build/install/mdgen /mdgen
run sed -i 's/UNIXSOCKET/TCPSOCKET/g' /opt/cloudhsm/data/application.cfg

env LD_LIBRARY_PATH=/opt/cloudhsm/lib
env HSM_PARTITION=PARTITION_1
env HSM_USER=user
env HSM_PASSWORD=password
env JAVA_HOME=/usr/lib/jvm/jdk-11.0.2
entrypoint ["/mdgen/bin/mdgen"]
