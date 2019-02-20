from amazonlinux:2.0.20190212

# Install AWS CloudHSM client and libs
run yum install -y wget tar gzip \
 && wget https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-latest.el7.x86_64.rpm \
 && yum install -y ./cloudhsm-client-latest.*.rpm \
 && rm ./cloudhsm-client-latest.*.rpm \
 && wget https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-jce-latest.el7.x86_64.rpm \
 && yum install -y ./cloudhsm-client-jce-latest.*.rpm \
 && rm ./cloudhsm-client-jce-latest.*.rpm \
 && wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz \
 && mkdir -p /usr/lib/jvm \
 && tar -C /usr/lib/jvm -xf ./openjdk-11.0.2*.tar.gz \
 && rm ./openjdk-11.0.2*.tar.gz \
 && sed -i 's/UNIXSOCKET/TCPSOCKET/g' /opt/cloudhsm/data/application.cfg

workdir /mdgen
env GRADLE_USER_HOME=/build/.gradle \
    LD_LIBRARY_PATH=/opt/cloudhsm/lib \
    HSM_PARTITION=PARTITION_1 \
    HSM_USER=user \
    HSM_PASSWORD=password \
    JAVA_HOME=/usr/lib/jvm/jdk-11.0.2
copy gradlew build.gradle settings.gradle ./
copy gradle/ gradle/
copy src/ src/
run ./gradlew --no-daemon installDist -x test

entrypoint ["/mdgen/build/install/mdgen/bin/mdgen"]
