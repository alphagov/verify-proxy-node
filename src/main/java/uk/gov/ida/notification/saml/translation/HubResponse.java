package uk.gov.ida.notification.saml.translation;

import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;

import java.util.Map;
import java.util.stream.Collectors;

public class HubResponse {
    private final Assertion mdsAssertion;
    private final Assertion authnAssertion;
    private final String pid;
    private final String statusCode;
    private final AuthnStatement authnStatement;
    private final Map<String, String> mdsAttributes;

    public HubResponse(Response response) {
        authnAssertion = response.getAssertions()
                .stream()
                .filter(a -> !a.getAuthnStatements().isEmpty())
                .findFirst()
                .orElseThrow(HubResponseException::new);

        mdsAssertion = response.getAssertions()
                .stream()
                .filter(a -> a.getAuthnStatements().isEmpty() && !a.getAttributeStatements().isEmpty())
                .findFirst()
                .orElseThrow(HubResponseException::new);

        pid = authnAssertion.getSubject().getNameID().getValue();

        authnStatement = authnAssertion.getAuthnStatements().get(0);

        mdsAttributes = mdsAssertion.getAttributeStatements().get(0).getAttributes()
                .stream()
                .filter(a -> ((XSAnyImpl) a.getAttributeValues().get(0)).getTextContent() != null)
                .collect(Collectors.toMap(
                        Attribute::getName,
                        a -> ((XSAnyImpl) a.getAttributeValues().get(0)).getTextContent())
                );

        statusCode = response.getStatus().getStatusCode().getValue();
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

    public AuthnStatement getAuthnStatement() {
        return authnStatement;
    }

    public String getStatusCode() {
        return statusCode;
    }
}
