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
    private final String eidasIssuerEntityId;

    @JsonCreator
    public GatewaySessionData(
            @JsonProperty("HUB_REQUEST_ID") String hubRequestId,
            @JsonProperty("EIDAS_REQUEST_ID") String eidasRequestId,
            @JsonProperty("EIDAS_DESTINATION") String eidasDestination,
            @JsonProperty("eidasRelayState") String eidasRelayState,
            @JsonProperty("eidasIssuerEntityId") String eidasIssuerEntityId) {
        this.hubRequestId = hubRequestId;
        this.eidasRequestId = eidasRequestId;
        this.eidasDestination = eidasDestination;
        this.eidasRelayState = eidasRelayState;
        this.eidasIssuerEntityId = eidasIssuerEntityId;
    }

    public GatewaySessionData(
            EidasSamlParserResponse eidasSamlParserResponse,
            AuthnRequestResponse vspResponse,
            String eidasRelayState) {
        this.hubRequestId = vspResponse.getRequestId();
        this.eidasRequestId = eidasSamlParserResponse.getRequestId();
        this.eidasDestination = eidasSamlParserResponse.getAssertionConsumerServiceLocation();
        this.eidasRelayState = eidasRelayState;
        this.eidasIssuerEntityId = eidasSamlParserResponse.getIssuerEntityId();
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

    public String getEidasIssuerEntityId() {
        return eidasIssuerEntityId;
    }
}
