package uk.gov.ida.notification.saml.translation;

import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HubResponse {
    private final String pid;
    private final String statusCode;
    private final String providedLoa;
    private final String responseId;
    private final String inResponseTo;
    private final Map<String, String> mdsAttributes;

    public HubResponse(String pid, String statusCode, String providedLoa, String responseId, String inResponseTo, Map<String, String> mdsAttributes) {
        this.pid = pid;
        this.statusCode = statusCode;
        this.providedLoa = providedLoa;
        this.responseId = responseId;
        this.inResponseTo = inResponseTo;
        this.mdsAttributes = mdsAttributes;
    }

    public static HubResponse fromResponse(Response response) {
        List<Assertion> assertions = response.getAssertions();
        Assertion authnAssertion = assertions
                .stream()
                .filter(a -> !a.getAuthnStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new HubResponseException("Hub Response has no authn assertion"));

        Assertion mdsAssertion = assertions
                .stream()
                .filter(a -> a.getAuthnStatements().isEmpty() && !a.getAttributeStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new HubResponseException("Hub Response has no MDS assertion"));

        String pid = authnAssertion.getSubject().getNameID().getValue();

        String providedLoa = authnAssertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();

        Map<String, String> mdsAttributes = mdsAssertion.getAttributeStatements().get(0).getAttributes().stream()
                .filter(a -> ((XSAnyImpl) a.getAttributeValues().get(0)).getTextContent() != null)
                .collect(Collectors.toMap(
                        Attribute::getName,
                        a -> ((XSAnyImpl) a.getAttributeValues().get(0)).getTextContent()));

        String statusCode = response.getStatus().getStatusCode().getValue();

        String responseId = response.getID();

        String inResponseTo = response.getInResponseTo();

        return new HubResponse(pid, statusCode, providedLoa, responseId, inResponseTo, mdsAttributes);
    }

    public String getPid() {
        return pid;
    }

    public String getMdsAttribute(String key) {
        return mdsAttributes.get(key);
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getProvidedLoa() {
        return providedLoa;
    }

    public String getInResponseTo() {
        return inResponseTo;
    }

    public String getResponseId() {
        return responseId;
    }
}
