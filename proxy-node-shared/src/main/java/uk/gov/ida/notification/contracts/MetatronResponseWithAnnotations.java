package uk.gov.ida.notification.contracts;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.net.URI;

public class MetatronResponseWithAnnotations implements MetatronResponse {

    @NotBlank
    @NotNull
    private String samlSigningCertX509;

    @NotBlank
    @NotNull
    private String samlEncryptionCertX509;

    @NotNull
    private URI destination;

    @NotBlank
    @NotNull
    private String entityId;

    @NotBlank
    @NotNull
    private String countryCode;

    // Needed for serialisation
    public MetatronResponseWithAnnotations() {}

    public MetatronResponseWithAnnotations(
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
