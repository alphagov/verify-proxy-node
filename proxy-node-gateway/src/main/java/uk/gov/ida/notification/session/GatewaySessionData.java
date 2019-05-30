package uk.gov.ida.notification.session;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.exceptions.SessionAttributeException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GatewaySessionData {

    @NotNull
    @NotEmpty
    @JsonProperty
    private final String hubRequestId;

    @NotNull
    @NotEmpty
    @JsonProperty
    private final String eidasRequestId;

    @NotNull
    @NotEmpty
    @JsonProperty
    private final String eidasDestination;

    @JsonProperty
    private final String eidasRelayState;

    @NotNull
    @NotEmpty
    @JsonProperty
    private final String eidasConnectorPublicKey;

    @JsonCreator
    public GatewaySessionData(
            @JsonProperty("HUB_REQUEST_ID") String hubRequestId,
            @JsonProperty("EIDAS_REQUEST_ID") String eidasRequestId,
            @JsonProperty("EIDAS_DESTINATION") String eidasDestination,
            @JsonProperty("eidasConnectorPublicKey") String eidasConnectorPublicKey,
            @JsonProperty("eidasRelayState") String eidasRelayState) {
        this.hubRequestId = hubRequestId;
        this.eidasRequestId = eidasRequestId;
        this.eidasDestination = eidasDestination;
        this.eidasConnectorPublicKey = eidasConnectorPublicKey;
        this.eidasRelayState = eidasRelayState;
    }

    public GatewaySessionData(
            EidasSamlParserResponse eidasSamlParserResponse,
            AuthnRequestResponse vspResponse,
            String eidasRelayState) {
        this.hubRequestId = vspResponse.getRequestId();
        this.eidasRequestId = eidasSamlParserResponse.getRequestId();
        this.eidasDestination = eidasSamlParserResponse.getDestination();
        this.eidasConnectorPublicKey = eidasSamlParserResponse.getConnectorEncryptionPublicCertificate();
        this.eidasRelayState = eidasRelayState;
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

            throw new SessionAttributeException(String.join(", ", collect), this.getHubRequestId(), getHubRequestId(), getEidasRequestId());
        }
    }

    private String combinePropertyAndMessage(ConstraintViolation violation) {
        return String.format("%s field %s", violation.getPropertyPath().toString(), violation.getMessage());
    }

    public String getHubRequestId() {
        return this.hubRequestId;
    }

    public String getEidasRequestId() {
        return this.eidasRequestId;
    }

    public String getEidasDestination() {
        return this.eidasDestination;
    }

    public String getEidasRelayState() {
        return this.eidasRelayState;
    }

    public String getEidasConnectorPublicKey() {
        return this.eidasConnectorPublicKey;
    }
}
