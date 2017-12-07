package uk.gov.ida.notification.saml.translation;

import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HubResponse {
    private final Assertion mdsAssertion;
    private final Assertion authnAssertion;
    private final String pid;
    private final String statusCode;
    private final String providedLoa;
    private final String responseId;
    private final String inResponseTo;
    private final Map<String, String> mdsAttributes;

    public HubResponse(Response response) {
        List<Assertion> assertions = response.getAssertions();
        authnAssertion = assertions
                .stream()
                .filter(a -> !a.getAuthnStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new HubResponseException("Hub Response has no authn assertion"));

        mdsAssertion = assertions
                .stream()
                .filter(a -> a.getAuthnStatements().isEmpty() && !a.getAttributeStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new HubResponseException("Hub Response has no MDS assertion"));

        pid = authnAssertion.getSubject().getNameID().getValue();

        providedLoa = authnAssertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();

        mdsAttributes = mdsAssertion.getAttributeStatements().get(0).getAttributes()
                .stream()
                .filter(a -> ((XSAnyImpl) a.getAttributeValues().get(0)).getTextContent() != null)
                .collect(Collectors.toMap(
                        Attribute::getName,
                        a -> ((XSAnyImpl) a.getAttributeValues().get(0)).getTextContent())
                );

        statusCode = response.getStatus().getStatusCode().getValue();

        responseId = response.getID();

        inResponseTo = response.getInResponseTo();
    }

    public Assertion getMdsAssertion() {
        return mdsAssertion;
    }

    public Assertion getAuthnAssertion() {
        return authnAssertion;
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
