package uk.gov.ida.notification.session;


import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.exceptions.SessionAttributeException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GatewaySessionData {
    @NotEmpty
    private final String hubRequestId;

    @NotEmpty
    private final String eidasRequestId;

    @NotEmpty
    private final String eidasDestination;

    private final String eidasRelayState;

    @NotEmpty
    private final String eidasConnectorPublicKey;

    public GatewaySessionData(
        EidasSamlParserResponse eidasSamlParserResponse,
        AuthnRequestResponse vspResponse,
        String eidasRelayState
    ) {
        this.hubRequestId = vspResponse.getRequestId();
        this.eidasRequestId = eidasSamlParserResponse.getRequestId();
        this.eidasDestination = eidasSamlParserResponse.getDestination();
        this.eidasRelayState = eidasRelayState;
        this.eidasConnectorPublicKey = eidasSamlParserResponse.getConnectorEncryptionPublicCertificate();
        validate();
    }

    private void validate() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<GatewaySessionData>> violations = validator.validate(this);
        if (violations.size() > 0) {
            List<String> collect = violations
                    .stream()
                    .map(this::combinePropertyAndMessage)
                    .sorted()
                    .collect(Collectors.toList());

            throw new SessionAttributeException(String.join(", ", collect), this.getHubRequestId());
        }
    }

    private String combinePropertyAndMessage(ConstraintViolation violation) {
        return String.format("%s field %s", violation.getPropertyPath().toString(), violation.getMessage());
    }

    public String getHubRequestId() { return this.hubRequestId; }

    public String getEidasRequestId() { return this.eidasRequestId; }

    public String getEidasDestination() { return this.eidasDestination; }

    public String getEidasRelayState() { return this.eidasRelayState; }

    public String getEidasConnectorPublicKey() { return this.eidasConnectorPublicKey; }
}
