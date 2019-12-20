# Base AWS Java app image

FROM amazoncorretto:11
ENV LANG C.UTF-8
WORKDIR /app

# Install AWS CloudHSM Java library if needed
ARG TALKS_TO_HSM=false
ENV LD_LIBRARY_PATH=/opt/cloudhsm/lib
ENV HSM_PARTITION=PARTITION_1

ADD https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-3.0.0-2.el7.x86_64.rpm .
ADD https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/EL7/cloudhsm-client-jce-3.0.0-2.el7.x86_64.rpm .

RUN if ${TALKS_TO_HSM}; then echo "Installing CloudHSM libs" \
    && yum install -y ./cloudhsm-client-*.rpm \
    && sed -i 's/UNIXSOCKET/TCPSOCKET/g' /opt/cloudhsm/data/application.cfg ; fi

RUN rm ./cloudhsm-client-*.rpm

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
