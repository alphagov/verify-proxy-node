#
#  Unit test image 
#
FROM gradle:jdk11

USER root

# Install softHSM and dependencies for OpenSC
RUN apt-get update && apt-get install -y softhsm pcscd libccid libpcsclite-dev libssl-dev libreadline-dev autoconf automake build-essential docbook-xsl xsltproc libtool pkg-config wget
ENV OPEN_SC_VERSION=0.19.0
RUN wget https://github.com/OpenSC/OpenSC/releases/download/${OPEN_SC_VERSION}/opensc-${OPEN_SC_VERSION}.tar.gz && \
    tar xfvz opensc-${OPEN_SC_VERSION}.tar.gz && \
    cd opensc-${OPEN_SC_VERSION} && \
    ./bootstrap && ./configure --prefix=/usr --sysconfdir=/etc/opensc && \
    make && make install && \
    cd .. && rm -rf opensc* && \
    rm -rf /var/lib/apt/lists/*

ENV VERIFY_USE_PUBLIC_BINARIES="true"

ENTRYPOINT ["/usr/bin/gradle", "--no-daemon"]
CMD []
