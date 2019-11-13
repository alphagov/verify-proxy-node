ARG JAVA_HOME=/usr/local/openjdk-11
FROM openjdk:11-jre-slim AS jre

FROM amazonlinux:2.0.20190508
WORKDIR /verify-service-provider

# Install Java
ARG JAVA_HOME
ENV JAVA_HOME $JAVA_HOME
ENV PATH=$PATH:$JAVA_HOME/bin
COPY --from=jre $JAVA_HOME $JAVA_HOME

# Instal VSP app
COPY build/install/verify-service-provider .

RUN rm verify-service-provider.yml
ADD https://raw.githubusercontent.com/alphagov/verify-proxy-node/release/single-country-legacy/proxy-node-vsp-config/verify-service-provider.yml verify-service-provider.yml

ENTRYPOINT ["bin/verify-service-provider"]
CMD ["server", "verify-service-provider.yml"]
