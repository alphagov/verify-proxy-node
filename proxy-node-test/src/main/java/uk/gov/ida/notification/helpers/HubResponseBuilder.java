package uk.gov.ida.notification.helpers;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import javax.xml.namespace.QName;

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

    private static <T extends XMLObject> T build(QName elementName) {
        return (T) XMLObjectSupport.buildXMLObject(elementName);
    }

    private StatusCode buildStatusCode(String statusCodeValue) {
        StatusCode statusCode = build(StatusCode.DEFAULT_ELEMENT_NAME);
        statusCode.setValue(statusCodeValue);
        return statusCode;
    }

    private Status createStatus(StatusCode statusCode) {
        Status status = build(Status.DEFAULT_ELEMENT_NAME);
        status.setStatusCode(statusCode);
        return status;
    }

    public HubResponseBuilder withStatus(String status) {
        responseBuilder.withStatus(createStatus(buildStatusCode(status)));
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
