from alpine:latest as build
workdir /softhsm

# Build softHSM source
arg version=2.5.0
run apk --update add opensc build-base wget sqlite-dev openssl-dev
run wget https://dist.opendnssec.org/source/softhsm-${version}.tar.gz && \
    tar xvf softhsm-${version}.tar.gz && \
    cd softhsm-${version} && \
    ./configure && \
    make && make install

# Build pkcs11-proxy
run apk --update add cmake bash linux-headers libseccomp-dev
run wget https://github.com/SUNET/pkcs11-proxy/archive/master.zip && \
    unzip master.zip && \
    cd pkcs11-proxy-master && \
    cmake . && make && make install

# Run image
from alpine:latest
workdir /softhsm

run apk --update add build-base opensc openssl libseccomp

copy --from=build /etc/softhsm2.conf* /etc/
copy --from=build /usr/local/lib/softhsm /usr/local/lib/softhsm
copy --from=build /usr/local/bin/softhsm* /usr/local/bin/

copy --from=build /usr/local/lib/libpkcs11-proxy* /usr/local/lib/
copy --from=build /usr/local/bin/pkcs11-daemon /usr/local/bin/

# Generate keypair to import into softHSM
run openssl ecparam -name secp384r1 | \
    openssl req -new -x509 -nodes -newkey ec:/dev/stdin -keyout hsm.key -out hsm.crt \
    -days 3650 -subj /CN=hsm && \
    mkdir -p /var/lib/softhsm/tokens && \
    softhsm2-util --init-token --slot 0 --label hsm --pin 1234 --so-pin 0000 && \
    softhsm2-util --import hsm.key --token hsm --label hsm --id BEEF --pin 1234 && \
    rm hsm.key && \
    cp /usr/local/lib/libpkcs11-proxy.so ./

expose 5656
env PKCS11_DAEMON_SOCKET="tcp://0.0.0.0:5656"
entrypoint ["/usr/local/bin/pkcs11-daemon", "/usr/local/lib/softhsm/libsofthsm2.so"]
cmd []
