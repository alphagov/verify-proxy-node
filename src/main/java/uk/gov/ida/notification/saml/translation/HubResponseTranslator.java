package uk.gov.ida.notification.saml.translation;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentFamilyNameType;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import se.litsec.eidas.opensaml.ext.attributes.DateOfBirthType;
import se.litsec.eidas.opensaml.ext.attributes.PersonIdentifierType;
import uk.gov.ida.notification.exceptions.HubResponseException;
import uk.gov.ida.notification.saml.SamlBuilder;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.Date;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.extensions.PersonName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HubResponseTranslator {
    private final String proxyNodeEntityId;
    private final String connectorNodeUrl;
    private final SecureRandomIdentifierGenerationStrategy idGeneratorStrategy;

    public HubResponseTranslator(String proxyNodeEntityId, String connectorNodeUrl) {
        this.proxyNodeEntityId = proxyNodeEntityId;
        this.connectorNodeUrl = connectorNodeUrl;
        idGeneratorStrategy = new SecureRandomIdentifierGenerationStrategy();
    }

    public Response translate(HubResponseContainer hubResponseContainer) {
        List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentGivenNameType.TYPE_NAME,
                this::combineFirstAndMiddleNames
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentFamilyNameType.TYPE_NAME,
                resp -> resp.getMdsAssertion().getMdsAttribute(IdaConstants.Attributes_1_1.Surname.NAME, PersonName.class).getValue()
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME, AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME, DateOfBirthType.TYPE_NAME,
                resp -> resp.getMdsAssertion().getMdsAttribute(IdaConstants.Attributes_1_1.DateOfBirth.NAME, Date.class).getValue()
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME, AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME, PersonIdentifierType.TYPE_NAME,
                resp -> resp.getAuthnStatement().getPid()
        ));

        List<Attribute> eidasAttributes = eidasAttributeBuilders
                .stream()
                .map(builder -> builder.build(hubResponseContainer))
                .collect(Collectors.toList());

        String eidasLoa = mapLoa(hubResponseContainer.getAuthnStatement().getProvidedLoa());

        Response eidasResponse = createEidasResponse(
                hubResponseContainer.getHubResponse().getStatusCode(),
                hubResponseContainer.getAuthnStatement().getPid(),
                eidasLoa,
                eidasAttributes,
                hubResponseContainer.getHubResponse().getInResponseTo(),
                hubResponseContainer.getHubResponse().getIssueInstant(),
                hubResponseContainer.getMdsAssertion().getIssueInstant(),
                hubResponseContainer.getAuthnStatement().getAuthnInstant()
        );

        return eidasResponse;
    }

    private String generateRandomId(){
        return idGeneratorStrategy.generateIdentifier(true);
    }

    private String combineFirstAndMiddleNames(HubResponseContainer hubResponseContainer) {
        List<PersonName> names = Arrays.asList(
                hubResponseContainer.getMdsAssertion().getMdsAttribute(IdaConstants.Attributes_1_1.Firstname.NAME, PersonName.class),
                hubResponseContainer.getMdsAssertion().getMdsAttribute(IdaConstants.Attributes_1_1.Middlename.NAME, PersonName.class));
        return names.stream()
                .filter(Objects::nonNull)
                .map(PersonName::getValue)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    private String mapLoa(String hubLoa) {
        switch(hubLoa) {
            case IdaAuthnContext.LEVEL_2_AUTHN_CTX:
                return EidasConstants.EIDAS_LOA_SUBSTANTIAL;
            default:
                throw new HubResponseException("Invalid level of assurance: " + hubLoa);
        }
    }

    private Response createEidasResponse(String statusCodeValue, String pid, String loa, List<Attribute> attributes, String inResponseTo, DateTime issueInstant, DateTime assertionIssueInstant, DateTime authnStatementAuthnInstant) {
        String responseId = generateRandomId();
        String assertionId = generateRandomId();

        Status status = createStatus(statusCodeValue);

        AuthnStatement authnStatement = createAuthnStatement(loa);
        authnStatement.setAuthnInstant(authnStatementAuthnInstant);

        Subject subject = createSubject(pid);
        AttributeStatement attributeStatement = createAttributeStatement(attributes);
        Issuer responseIssuer = createIssuer();
        Issuer assertionIssuer = createIssuer();
        Assertion assertion = createAssertion(authnStatement, subject, attributeStatement, assertionIssuer, assertionId, assertionIssueInstant);

        Response response = SamlBuilder.build(Response.DEFAULT_ELEMENT_NAME);
        response.setStatus(status);
        response.setIssuer(responseIssuer);
        response.getAssertions().add(assertion);
        response.setID(responseId);
        response.setInResponseTo(inResponseTo);
        response.setDestination(connectorNodeUrl);
        response.setIssueInstant(issueInstant);

        return response;
    }

    private Assertion createAssertion(AuthnStatement authnStatement, Subject subject, AttributeStatement attributeStatement, Issuer assertionIssuer, String assertionId, DateTime assertionIssueInstant) {
        Assertion assertion = SamlBuilder.build(Assertion.DEFAULT_ELEMENT_NAME);
        assertion.getAuthnStatements().add(authnStatement);
        assertion.setSubject(subject);
        assertion.getAttributeStatements().add(attributeStatement);
        assertion.setIssuer(assertionIssuer);
        assertion.setID(assertionId);
        assertion.setIssueInstant(assertionIssueInstant);
        return assertion;
    }

    private AttributeStatement createAttributeStatement(List<Attribute> attributes) {
        AttributeStatement attributeStatement = SamlBuilder.build(AttributeStatement.DEFAULT_ELEMENT_NAME);
        attributeStatement.getAttributes().addAll(attributes);
        return attributeStatement;
    }

    private AuthnStatement createAuthnStatement(String loa) {
        AuthnStatement authnStatement = SamlBuilder.build(AuthnStatement.DEFAULT_ELEMENT_NAME);
        AuthnContext authnContext = SamlBuilder.build(AuthnContext.DEFAULT_ELEMENT_NAME);
        AuthnContextClassRef authnContextClassRef = SamlBuilder.build(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(loa);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        return authnStatement;
    }

    private Subject createSubject(String pid) {
        Subject subject = SamlBuilder.build(Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = SamlBuilder.build(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(pid);
        nameID.setFormat(NameIDType.PERSISTENT);
        subject.setNameID(nameID);
        return subject;
    }

    private Status createStatus(String statusCodeValue) {
        Status status = SamlBuilder.build(Status.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = SamlBuilder.build(StatusCode.DEFAULT_ELEMENT_NAME);
        statusCode.setValue(statusCodeValue);
        status.setStatusCode(statusCode);
        return status;
    }

    private Issuer createIssuer() {
        Issuer responseIssuer = SamlBuilder.build(Issuer.DEFAULT_ELEMENT_NAME);
        responseIssuer.setFormat(NameIDType.ENTITY);
        responseIssuer.setValue(proxyNodeEntityId);
        return responseIssuer;
    }
}
