package uk.gov.ida.notification.saml.translation;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import uk.gov.ida.notification.saml.SamlBuilder;

import java.util.List;

public class EidasResponseBuilder {
    private final String statusCodeValue;
    private final String pid;
    private final String loa;
    private final List<Attribute> attributes;

    public EidasResponseBuilder(String statusCodeValue, String pid, String loa, List<Attribute> attributes) {
        this.statusCodeValue = statusCodeValue;
        this.pid = pid;
        this.loa = loa;
        this.attributes = attributes;
    }

    public Response build() {
        Response response = SamlBuilder.build(Response.DEFAULT_ELEMENT_NAME);
        Status status = SamlBuilder.build(Status.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = SamlBuilder.build(StatusCode.DEFAULT_ELEMENT_NAME);
        Assertion assertion = SamlBuilder.build(Assertion.DEFAULT_ELEMENT_NAME);
        AuthnStatement authnStatement = SamlBuilder.build(AuthnStatement.DEFAULT_ELEMENT_NAME);
        AuthnContext authnContext = SamlBuilder.build(AuthnContext.DEFAULT_ELEMENT_NAME);
        AuthnContextClassRef authnContextClassRef = SamlBuilder.build(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        Subject subject = SamlBuilder.build(Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = SamlBuilder.build(NameID.DEFAULT_ELEMENT_NAME);
        AttributeStatement attributeStatement = SamlBuilder.build(AttributeStatement.DEFAULT_ELEMENT_NAME);

        statusCode.setValue(statusCodeValue);
        status.setStatusCode(statusCode);

        authnContextClassRef.setAuthnContextClassRef(loa);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);

        nameID.setValue(pid);
        nameID.setFormat(NameIDType.PERSISTENT);
        subject.setNameID(nameID);

        attributeStatement.getAttributes().addAll(attributes);

        assertion.getAuthnStatements().add(authnStatement);
        assertion.setSubject(subject);
        assertion.getAttributeStatements().add(attributeStatement);

        response.setStatus(status);
        response.getAssertions().add(assertion);

        return response;
    }
}
