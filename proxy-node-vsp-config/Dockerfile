FROM amazonlinux:2.0.20191016.0
WORKDIR /verify-service-provider

# Install Java
RUN yum install -y java-11-amazon-corretto

# Instal VSP app
COPY build/install/verify-service-provider .

RUN rm verify-service-provider.yml
ADD https://raw.githubusercontent.com/alphagov/verify-proxy-node/master/proxy-node-vsp-config/verify-service-provider.yml verify-service-provider.yml

ENTRYPOINT ["bin/verify-service-provider"]
CMD ["server", "verify-service-provider.yml"]
