from openjdk:11-jre-slim as build
workdir /app

# Build softHSM source
arg version=2.5.0

run apt-get update && \
    apt-get install -y \
    opensc build-essential wget libsqlite3-dev libssl-dev \
    cmake libseccomp-dev libengine-gost-openssl1.1

run wget https://dist.opendnssec.org/source/softhsm-${version}.tar.gz && \
    tar xvf softhsm-${version}.tar.gz && \
    cd softhsm-${version} && \
    ./configure && \
    make && make install

# Build pkcs11-proxy
run wget https://github.com/SUNET/pkcs11-proxy/archive/master.zip && \
    unzip master.zip && \
    cd pkcs11-proxy-master && \
    cmake . && make && make install

# Run image
from openjdk:11-jre-slim
workdir /app
volume /softhsm

run apt-get update && \
    apt-get install -y build-essential opensc openssl libseccomp2 dumb-init

copy --from=build /etc/softhsm2.conf* /etc/
copy --from=build /usr/local/lib/softhsm /usr/local/lib/softhsm
copy --from=build /usr/local/bin/softhsm* /usr/local/bin/

copy --from=build /usr/local/lib/libpkcs11-proxy* /usr/local/lib/
copy --from=build /usr/local/bin/pkcs11-daemon /usr/local/bin/

copy ./softhsm/init.sh init.sh
run chmod +x init.sh

expose 5656
env PKCS11_DAEMON_SOCKET="tcp://0.0.0.0:5656"
entrypoint ["dumb-init", "--"]
cmd ["/app/init.sh"]
