package uk.gov.ida.notification.helpers;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;

public class HubResponseBuilder {

    private ResponseBuilder responseBuilder;

    public HubResponseBuilder() {
        responseBuilder = new ResponseBuilder();
    }

    public static HubResponseBuilder aHubResponse() {
        return new HubResponseBuilder();
    }

    public HubResponseBuilder addEncryptedAssertion(EncryptedAssertion assertion) {
        responseBuilder.addEncryptedAssertion(assertion);
        return this;
    }

    public HubResponseBuilder withIssuer(String issuer) {
        responseBuilder.withIssuer(anIssuer().withIssuerId(issuer).build());
        return this;
    }

    public HubResponseBuilder withDestination(String destination) {
        responseBuilder.withDestination(destination);
        return this;
    }

    public HubResponseBuilder withInResponseTo(String requestId) {
        responseBuilder.withInResponseTo(requestId);
        return this;
    }

    public Response build() throws MarshallingException, SignatureException {
        return responseBuilder
                .withoutSignatureElement()
                .withoutSigning()
                .build();
    }

    public Response buildSigned(Credential signingCredential) throws MarshallingException, SignatureException {
        return responseBuilder
            .withSigningCredential(signingCredential)
            .build();
    }
}
