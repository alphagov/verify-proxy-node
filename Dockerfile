# Base AWS Java app image

FROM amazoncorretto:11
ENV LANG C.UTF-8
WORKDIR /app

# Install AWS CloudHSM Java library if needed
ARG TALKS_TO_HSM=false
ENV LD_LIBRARY_PATH=/opt/cloudhsm/lib
ENV HSM_PARTITION=PARTITION_1

RUN if ${TALKS_TO_HSM}; then echo "Installing CloudHSM libs" \
    && curl -Os https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-latest.el7.x86_64.rpm \
    && curl -Os https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-jce-latest.el7.x86_64.rpm \
    && yum install -y -q ./cloudhsm-client-*.rpm \
    && sed -i 's/UNIXSOCKET/TCPSOCKET/g' /opt/cloudhsm/data/application.cfg \
    && rm ./cloudhsm-client-*.rpm ; fi

# Install Proxy Node app
ARG TINI_VERSION=v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

ARG component
ENV COMPONENT $component

COPY ${component}/build/install/${component} .
ENV CONFIG_FILE config.yml

ENTRYPOINT ["/tini", "--"]
CMD "bin/$COMPONENT"
