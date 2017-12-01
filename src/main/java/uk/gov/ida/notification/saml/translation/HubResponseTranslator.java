package uk.gov.ida.notification.saml.translation;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
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
import uk.gov.ida.notification.saml.SamlBuilder;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HubResponseTranslator {
    private static final Logger LOG = Logger.getLogger(HubResponseTranslator.class.getName());

    private final String proxyNodeEntityId;
    private final String connectorNodeUrl;
    private final SamlParser samlParser;
    private final SamlMarshaller samlMarshaller;
    private final SecureRandomIdentifierGenerationStrategy idGeneratorStrategy;

    public HubResponseTranslator(String proxyNodeEntityId, String connectorNodeUrl, SamlParser samlParser, SamlMarshaller samlMarshaller) {
        this.proxyNodeEntityId = proxyNodeEntityId;
        this.connectorNodeUrl = connectorNodeUrl;
        this.samlParser = samlParser;
        this.samlMarshaller = samlMarshaller;

        idGeneratorStrategy = new SecureRandomIdentifierGenerationStrategy();
    }

    public String translate(String decodedHubResponse) {
        HubResponse hubResponse = new HubResponse(samlParser.parseSamlString(decodedHubResponse));

        LOG.info("[Hub Response] ID: " + hubResponse.getResponseId());
        LOG.info("[Hub Response] In response to: " + hubResponse.getInResponseTo());
        LOG.info("[Hub Response] Provided level of assurance: " + hubResponse.getProvidedLoa());

        List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentGivenNameType.TYPE_NAME,
                resp -> String.join(" ", resp.getMdsAttribute(IdaConstants.Attributes_1_1.Firstname.NAME), resp.getMdsAttribute(IdaConstants.Attributes_1_1.Middlename.NAME))
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentFamilyNameType.TYPE_NAME,
                resp -> resp.getMdsAttribute(IdaConstants.Attributes_1_1.Surname.NAME)
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME, AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME, DateOfBirthType.TYPE_NAME,
                resp -> resp.getMdsAttribute(IdaConstants.Attributes_1_1.DateOfBirth.NAME)
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME, AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME, PersonIdentifierType.TYPE_NAME,
                HubResponse::getPid
        ));

        List<Attribute> eidasAttributes = eidasAttributeBuilders
                .stream()
                .map(builder -> builder.build(hubResponse))
                .collect(Collectors.toList());

        String eidasLoa = mapLoa(hubResponse.getProvidedLoa());

        Response eidasResponse = createEidasResponse(
                hubResponse.getStatusCode(),
                hubResponse.getPid(),
                eidasLoa,
                eidasAttributes,
                idGeneratorStrategy.generateIdentifier(true),
                hubResponse.getInResponseTo()
        );

        LOG.info("[eIDAS Response] ID: " + eidasResponse.getID());
        LOG.info("[eIDAS Response] In response to: " + eidasResponse.getInResponseTo());

        return samlMarshaller.samlObjectToString(eidasResponse);
    }

    private String mapLoa(String hubLoa) {
        switch(hubLoa) {
            case IdaAuthnContext.LEVEL_2_AUTHN_CTX:
                return EidasConstants.EIDAS_LOA_SUBSTANTIAL;
            default:
                throw new HubResponseException("Invalid level of assurance: " + hubLoa);
        }
    }

    private Response createEidasResponse(String statusCodeValue, String pid, String loa, List<Attribute> attributes, String responseId, String inResponseTo) {
        Status status = createStatus(statusCodeValue);
        AuthnStatement authnStatement = createAuthnStatement(loa);
        Subject subject = createSubject(pid);
        AttributeStatement attributeStatement = createAttributeStatement(attributes);
        Issuer responseIssuer = createIssuer();
        Issuer assertionIssuer = createIssuer();
        Assertion assertion = createAssertion(authnStatement, subject, attributeStatement, assertionIssuer);

        Response response = SamlBuilder.build(Response.DEFAULT_ELEMENT_NAME);
        response.setStatus(status);
        response.setIssuer(responseIssuer);
        response.getAssertions().add(assertion);
        response.setID(responseId);
        response.setInResponseTo(inResponseTo);
        response.setDestination(connectorNodeUrl);

        return response;
    }

    private Assertion createAssertion(AuthnStatement authnStatement, Subject subject, AttributeStatement attributeStatement, Issuer assertionIssuer) {
        Assertion assertion = SamlBuilder.build(Assertion.DEFAULT_ELEMENT_NAME);
        assertion.getAuthnStatements().add(authnStatement);
        assertion.setSubject(subject);
        assertion.getAttributeStatements().add(attributeStatement);
        assertion.setIssuer(assertionIssuer);
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
