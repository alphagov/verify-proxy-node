# Custom VSP image using Amazon JDK

FROM eidas/verify-service-provider
WORKDIR /verify-service-provider

# Replace config file with the local version
RUN rm verify-service-provider.yml
COPY verify-service-provider-local-hub.yml verify-service-provider.yml

ENTRYPOINT ["bin/verify-service-provider"]
CMD ["server", "verify-service-provider.yml"]
