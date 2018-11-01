package uk.gov.ida.notification.saml.translation;

import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.saml.security.validators.ValidatedResponse;

public class HubResponseContainer {
    private final HubResponse hubResponse;
    private final HubMdsAssertion mdsAssertion;
    private final HubAuthnAssertion authnAssertion;

    public HubResponseContainer(HubResponse hubResponse, HubMdsAssertion mdsAssertion, HubAuthnAssertion authnAssertion) {
        this.hubResponse = hubResponse;
        this.mdsAssertion = mdsAssertion;
        this.authnAssertion = authnAssertion;
    }

    public HubAuthnAssertion getAuthnAssertion() {
        return authnAssertion;
    }

    public HubResponse getHubResponse() {
        return hubResponse;
    }

    public HubMdsAssertion getMdsAssertion() {
        return mdsAssertion;
    }

    public static HubResponseContainer from(ValidatedResponse validatedResponse, ValidatedAssertions validatedAssertions) {
        HubResponse hubResponse = HubResponse.from(validatedResponse);

        HubMdsAssertion mdsAssertion = HubMdsAssertion.fromAssertion(validatedAssertions.getMatchingDatasetAssertion().get());
        HubAuthnAssertion hubAuthnAssertion = HubAuthnAssertion.fromAssertion(validatedAssertions.getAuthnStatementAssertion().get());

        return new HubResponseContainer(hubResponse, mdsAssertion, hubAuthnAssertion);
    }
}
