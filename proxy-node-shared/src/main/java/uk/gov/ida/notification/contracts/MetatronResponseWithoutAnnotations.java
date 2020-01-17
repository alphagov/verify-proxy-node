package uk.gov.ida.notification.contracts;

import java.net.URI;

public class MetatronResponseWithoutAnnotations implements MetatronResponse {

    private String samlSigningCertX509;
    private String samlEncryptionCertX509;
    private URI destination;
    private String entityId;
    private String countryCode;

    // Needed for serialisation
    public MetatronResponseWithoutAnnotations() {}

    public MetatronResponseWithoutAnnotations(
            String samlSigningCertX509,
            String samlEncryptionCertX509,
            URI destination,
            String entityId,
            String countryCode) {
        this.samlSigningCertX509 = samlSigningCertX509;
        this.samlEncryptionCertX509 = samlEncryptionCertX509;
        this.destination = destination;
        this.entityId = entityId;
        this.countryCode = countryCode;
    }

    public String getSamlSigningCertX509() { return this.samlSigningCertX509; }

    public String getSamlEncryptionCertX509() { return this.samlEncryptionCertX509; }

    public URI getDestination() { return this.destination; }

    public String getEntityId() { return this.entityId; }

    public String getCountryCode() { return this.countryCode; }
}
