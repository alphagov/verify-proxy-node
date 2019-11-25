# Custom VSP image using Amazon JDK

FROM amazoncorretto:11
ENV LANG C.UTF-8
WORKDIR /verify-service-provider

# Instal VSP app
COPY build/install/verify-service-provider .

RUN rm verify-service-provider.yml
ADD https://raw.githubusercontent.com/alphagov/verify-proxy-node/release/single-country-legacy/proxy-node-vsp-config/verify-service-provider.yml verify-service-provider.yml

ENTRYPOINT ["bin/verify-service-provider"]
CMD ["server", "verify-service-provider.yml"]
