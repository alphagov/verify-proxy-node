package uk.gov.ida.notification.contracts.metadata;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.ida.notification.validations.ValidPEM;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class CountryMetadataResponse {

    @NotBlank
    @ValidPEM
    private String samlSigningCertX509;

    @NotBlank
    @ValidPEM
    private String samlEncryptionCertX509;

    @Valid
    @NotNull
    private List<AssertionConsumerService> assertionConsumerServices;

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
            List<AssertionConsumerService> assertionConsumerServices,
            String entityId,
            String countryCode) {
        this.samlSigningCertX509 = samlSigningCertX509;
        this.samlEncryptionCertX509 = samlEncryptionCertX509;
        this.assertionConsumerServices = assertionConsumerServices;
        this.entityId = entityId;
        this.countryCode = countryCode;
    }

    public String getSamlSigningCertX509() { return this.samlSigningCertX509; }

    public String getSamlEncryptionCertX509() { return this.samlEncryptionCertX509; }

    public List<AssertionConsumerService> getAssertionConsumerServices() { return this.assertionConsumerServices; }

    public String getEntityId() { return this.entityId; }

    public String getCountryCode() { return this.countryCode; }
}
