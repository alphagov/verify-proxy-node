# Metatron

The Metatron is a Proxy Node microservice responsible for:
* fetching eIDAS Connector Node metadata for connected countries,
* validating that metadata, 
* providing a view of that metadata via a REST API.

Country metadata configuration is set via the repository <https://github.com/alphagov/verify-eidas-config>.

The Metatron is used by the `translator` and `eidas-saml-parser` microservices. 

## Fetching Country Metadata
A `MetadataResolver` for each country is responsible for fetching a country's metadata from a URL e.g. <https://eidas.minez.nl/EidasNodeC/ConnectorMetadata>. Each resolver runs from a timer thread.

## Validating Metadata
A Countries' Metadata is valid when:
* it is signed using that country's truststore
* it is within its expiry date
* signing and encryption certificates are present and valid
* it has an Assertion Consumer Service URL and an Entity Descriptor

## Metadata API
Metatron clients may call `GET /metadata/{entityId}` to retrieve a json payload of metadata for that entity id (country). The json returned is a marshalled version of the `CountryMetadataResponse`.

The status codes of a successful response is 200.

A client will receive a 400 response when an entity id is not enabled or not configured.

A client will receive a 500 response if the metadata for an entity id is unavailable or invalid.


## Unhealthy Country Metadata
If metadata for a country is unavailable or invalid, the Metatron API will indicate this with a 500 response status code.

There is no storage of the state of metadata across instances (pods), the state on each instance will eventually become consistent.

If no metadata is available the Metatron will remain healthy. Metrics and Alerts must indicate that country metadata is invalid.