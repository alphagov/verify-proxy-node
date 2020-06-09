package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.junit.DropwizardClientRule;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.joda.time.DateTime;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentFamilyNameType;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.apprule.base.StubConnectorAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.AppRule;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.helpers.X509CredentialFactory;
import uk.gov.ida.notification.saml.EidasAttributeBuilder;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

public class EidasResponseValidatorAppRuleTests extends StubConnectorAppRuleTestBase {

    private static final DropwizardClientRule metadataClientRule = createTestMetadataClientRule();
    private static final AppRule<StubConnectorConfiguration> stubConnectorAppRule = createStubConnectorAppRule(metadataClientRule);

    @ClassRule
    public static final RuleChain orderedRules = RuleChain.outerRule(metadataClientRule).around(stubConnectorAppRule);

    private static final BasicX509Credential encryptionCredential;

    static {
        try {
            encryptionCredential = X509CredentialFactory.build(TEST_PUBLIC_CERT, TEST_PRIVATE_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldUseCorrectEntityIdInAuthnRequest() throws IOException, URISyntaxException {
        final AuthnRequest authnRequest = getEidasAuthnRequest();

        assertThat(authnRequest.getIssuer().getValue()).isEqualTo(ENTITY_ID);
    }

    @Test
    public void shouldGetStubConnectorMetadata() throws URISyntaxException, XMLParserException, UnmarshallingException {
        EntityDescriptor connectorMetadata = getConnectorMetadata();
        SPSSODescriptor spssoDescriptor = connectorMetadata.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        Organization organization = connectorMetadata.getOrganization();
        AssertionConsumerService assertionConsumerService = spssoDescriptor.getAssertionConsumerServices().get(0);
        assertThat(spssoDescriptor.getWantAssertionsSigned()).isTrue();
        assertThat(connectorMetadata.getEntityID()).isEqualTo(ENTITY_ID);
        assertThat(assertionConsumerService.getLocation()).isEqualTo(ACS_URL);
        assertThat(organization.getOrganizationNames().get(0).getValue()).isEqualTo(ENTITY_ORG_NAME);
        assertThat(organization.getDisplayNames().get(0).getValue()).isEqualTo(ENTITY_ORG_DISPLAY_NAME);
        assertThat(organization.getURLs().get(0).getValue()).isEqualTo(ENTITY_ORG_URL);
        assertThat(connectorMetadata.isSigned()).isTrue();
        assertThat(connectorMetadata.isValid()).isTrue();
    }

    @Test
    public void shouldReturnValidSamlResponse() throws Exception {
        String authnId = getAuthnRequestIdFromSession();

        Response response = getEidasSamlMessage(authnId);
        encryptAssertions(response);
        signResponse(response);

        String validSamlMessage = responseToString(response);

        checkValidity(validSamlMessage, "VALID");
    }

    @Test
    public void shouldReturnInvalidSamlResponse() throws Exception {
        String authnId = getAuthnRequestIdFromSession();
        Response unsignedSamlResponse = getEidasSamlMessage(authnId);
        String invalidSamlMessage = responseToString(unsignedSamlResponse);

        checkValidity(invalidSamlMessage, "INVALID");
    }

    @Test
    public void shouldRejectMalformedBase64Saml() throws Exception {
        String authnId = getAuthnRequestIdFromSession();

        Response response = getEidasSamlMessage(authnId);
        encryptAssertions(response);
        signResponse(response);

        String validSamlMessage = responseToString(response);
        String invalidSamlMessage = "not-the-xml-opening-tag" + validSamlMessage;
        checkValidity(invalidSamlMessage, "INVALID");
    }

    @Test
    public void shouldBeInvalidAsNoneSeenAuthId() throws Exception {
        String authnId = UUID.randomUUID().toString();

        Response response = getEidasSamlMessage(authnId);
        signResponse(response);

        String validSamlMessage = responseToString(response);

        checkValidity(validSamlMessage, "INDETERMINATE");
    }

    private AuthnRequest getEidasAuthnRequest() throws IOException, URISyntaxException {
        String html = getEidasRequest(stubConnectorAppRule);
        String decodedSaml = HtmlHelpers.getValueFromForm(html, "SAMLRequest");
        return new SamlParser().parseSamlString(decodedSaml);
    }

    private EntityDescriptor getConnectorMetadata() throws URISyntaxException, UnmarshallingException, XMLParserException {
        String html = getConnectorMetadata(stubConnectorAppRule);
        return ObjectUtils.unmarshall(new ByteArrayInputStream(html.getBytes()), EntityDescriptor.class);
    }

    private String getAuthnRequestIdFromSession() throws IOException, URISyntaxException {
        return getEidasAuthnRequest().getID();
    }

    private void checkValidity(String samlMessage, String validity) throws URISyntaxException {
        String html = postEidasResponse(stubConnectorAppRule, samlMessage);
        assertThat(html).contains("Saml Validity: " + validity);
    }

    private Response getEidasSamlMessage(String authid) {

        Attribute firstName = new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME,
                CurrentGivenNameType.TYPE_NAME,
                "Jazzy"
        ).build();

        Attribute lastName = new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME,
                CurrentFamilyNameType.TYPE_NAME,
                "Harrold"
        ).build();

        ArrayList<Attribute> eidasAttributes = new ArrayList<>();

        eidasAttributes.add(firstName);
        eidasAttributes.add(lastName);

        DateTime now = DateTime.now();

        return EidasResponseBuilder.instance()
                .withIssuer("http://stub-connector/")
                .withStatus(StatusCode.SUCCESS)
                .withInResponseTo(authid)
                .withIssueInstant(now)
                .withDestination("http://stub-connector/SAML2/Response/POST")
                .withAssertionSubject(UUID.randomUUID().toString(), authid, "http://stub-connector/SAML2/Response/POST")
                .addAssertionAuthnStatement(EidasConstants.EIDAS_LOA_SUBSTANTIAL, now)
                .addAssertionAttributeStatement(eidasAttributes)
                .withAssertionConditions("http://localhost:5000/Metadata")
                .build();
    }

    private void signResponse(Response response) throws Exception {
        SamlObjectSigner signer = new SamlObjectSigner(X509CredentialFactory.build(
                TEST_RP_PUBLIC_SIGNING_CERT, TEST_RP_PRIVATE_SIGNING_KEY),
                SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                1L);

        signer.sign(response, "response-id");
    }

    private void encryptAssertions(Response response) {
        final ResponseAssertionEncrypter encrypter = new ResponseAssertionEncrypter(new BasicX509Credential(encryptionCredential.getEntityCertificate()));
        encrypter.encrypt(response);
    }

    private String responseToString(Response response) {
        SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
        return marshaller.transformToString(response);
    }
}
