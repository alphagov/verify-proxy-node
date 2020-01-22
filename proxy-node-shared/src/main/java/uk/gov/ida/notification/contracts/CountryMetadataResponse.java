package uk.gov.ida.notification.contracts;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.ida.notification.validations.ValidDestinationUri;
import uk.gov.ida.notification.validations.ValidPEM;

import javax.validation.constraints.NotNull;
import java.net.URI;

public class CountryMetadataResponse {

    @NotBlank
    @ValidPEM
    private String samlSigningCertX509;

    @NotBlank
    @ValidPEM
    private String samlEncryptionCertX509;

    @NotNull
    @ValidDestinationUri
    private URI destination;

    @NotBlank
    private String entityId;

    @NotBlank
    @Length(min = 2, max = 2)
    private String countryCode;

    // Needed for serialisation
    public CountryMetadataResponse() {}

    public CountryMetadataResponse(
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
