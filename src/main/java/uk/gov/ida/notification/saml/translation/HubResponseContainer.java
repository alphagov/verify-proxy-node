package uk.gov.ida.notification.saml.translation;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;

import java.util.List;

public class HubResponseContainer {
    private final HubResponse hubResponse;
    private final HubMdsAssertion mdsAssertion;
    private final HubAuthnStatement authnStatement;

    public HubResponseContainer(HubResponse hubResponse, HubMdsAssertion mdsAssertion, HubAuthnStatement authnStatement) {
        this.hubResponse = hubResponse;
        this.mdsAssertion = mdsAssertion;
        this.authnStatement = authnStatement;
    }


    public static HubResponseContainer fromResponse(Response response) {
        HubResponse hubResponse = HubResponse.fromResponse(response);

        List<Assertion> assertions = response.getAssertions();
        HubMdsAssertion mdsAssertion = HubMdsAssertion.fromAssertions(assertions);
        HubAuthnStatement hubAuthnStatement = HubAuthnStatement.fromAssertions(assertions);

        return new HubResponseContainer(hubResponse, mdsAssertion, hubAuthnStatement);
    }

    public HubAuthnStatement getAuthnStatement() {
        return authnStatement;
    }

    public HubResponse getHubResponse() {
        return hubResponse;
    }

    public HubMdsAssertion getMdsAssertion() {
        return mdsAssertion;
    }
}
