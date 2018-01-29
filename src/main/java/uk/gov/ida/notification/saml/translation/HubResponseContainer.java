package uk.gov.ida.notification.saml.translation;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;

import java.util.List;

public class HubResponseContainer {
    private final HubResponse hubResponse;
    private final HubMdsAssertion mdsAssertion;
    private final HubAuthnAssertion authnAssertion;

    public HubResponseContainer(HubResponse hubResponse, HubMdsAssertion mdsAssertion, HubAuthnAssertion authnAssertion) {
        this.hubResponse = hubResponse;
        this.mdsAssertion = mdsAssertion;
        this.authnAssertion = authnAssertion;
    }

    public static HubResponseContainer fromResponse(Response response) {
        HubResponse hubResponse = HubResponse.fromResponse(response);

        List<Assertion> assertions = response.getAssertions();
        HubMdsAssertion mdsAssertion = HubMdsAssertion.fromAssertions(assertions);
        HubAuthnAssertion hubAuthnAssertion = HubAuthnAssertion.fromAssertions(assertions);

        return new HubResponseContainer(hubResponse, mdsAssertion, hubAuthnAssertion);
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
}
