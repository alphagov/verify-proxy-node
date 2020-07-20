package uk.gov.ida.notification.translator.apprule;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.apache.xml.security.signature.XMLSignature;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.LoggerFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.notification.apprule.rules.AppRule;
import uk.gov.ida.notification.configuration.CredentialConfiguration;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseTestAssertions;
import uk.gov.ida.notification.helpers.BasicCredentialBuilder;
import uk.gov.ida.notification.helpers.HubAssertionBuilder;
import uk.gov.ida.notification.helpers.HubResponseBuilder;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.translator.apprule.base.TranslatorAppRuleTestBase;
import uk.gov.ida.notification.translator.configuration.TranslatorConfiguration;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;
import uk.gov.ida.saml.security.CredentialFactorySignatureValidator;
import uk.gov.ida.saml.security.SigningCredentialFactory;

import javax.ws.rs.client.Entity;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_EGRESS;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_INGRESS;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;

@RunWith(MockitoJUnitRunner.class)
public class HubResponseTranslatorAppRuleTests extends TranslatorAppRuleTestBase {

    private static final String PROXY_NODE_ENTITY_ID = "http://proxy-node.uk";
    private static final String EIDAS_TEST_CONNECTOR_DESTINATION = "http://proxy-node/SAML2/SSO/Response";
    private static final String ALTERNATIVE_EIDAS_TEST_CONNECTOR_DESTINATION = "http://alternative-proxy-node/SAML2/SSO/Response";

    private static final SamlObjectMarshaller MARSHALLER = new SamlObjectMarshaller();
    private static final X509CertificateFactory X_509_CERTIFICATE_FACTORY = new X509CertificateFactory();

    private static final BasicCredential idpSigningCredential;
    private static final BasicCredential hubSigningCredential;
    private static final Credential eidasDecryptingCredential;
    private static final Credential hubAssertionsEncryptionCredential;

    static {
        try {
            idpSigningCredential = BasicCredentialBuilder.instance()
                    .withPublicSigningCert(STUB_IDP_PUBLIC_PRIMARY_CERT)
                    .withPrivateSigningKey(STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY)
                    .build();

            hubSigningCredential = BasicCredentialBuilder.instance()
                    .withPublicSigningCert(HUB_TEST_PUBLIC_SIGNING_CERT)
                    .withPrivateSigningKey(HUB_TEST_PRIVATE_SIGNING_KEY)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        hubAssertionsEncryptionCredential = new BasicCredential(
                X_509_CERTIFICATE_FACTORY.createCertificate(HUB_TEST_PUBLIC_ENCRYPTION_CERT).getPublicKey());

        eidasDecryptingCredential =
                new TestCredentialFactory(
                        STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                        STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                ).getDecryptingCredential();
    }

    @Mock
    private Appender<ILoggingEvent> appender;

    @Captor
    private ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor;

    private EncryptedAssertion authnAssertion;
    private EncryptedAssertion matchingDatasetAssertion;

    @Before
    public void setUp() {
        buildAssertions();
    }

    private void buildAssertions() {
        authnAssertion = HubAssertionBuilder.anAuthnStatementAssertion()
                .withSignature(idpSigningCredential, STUB_IDP_PUBLIC_PRIMARY_CERT)
                .withIssuer(TestEntityIds.STUB_IDP_ONE)
                .withSubject(PROXY_NODE_ENTITY_ID, ResponseBuilder.DEFAULT_REQUEST_ID)
                .buildEncrypted(hubAssertionsEncryptionCredential);

        matchingDatasetAssertion = HubAssertionBuilder.aMatchingDatasetAssertion()
                .withSignature(idpSigningCredential, STUB_IDP_PUBLIC_PRIMARY_CERT)
                .withIssuer(TestEntityIds.STUB_IDP_ONE)
                .withSubject(PROXY_NODE_ENTITY_ID, ResponseBuilder.DEFAULT_REQUEST_ID)
                .buildEncrypted(hubAssertionsEncryptionCredential);
    }

    @Test
    public void shouldReturnASignedEidasResponse() throws Exception {
        CredentialConfiguration credentialConfiguration = translatorAppRule.getConfiguration().getCredentialConfiguration();
        Credential signingCredential = credentialConfiguration.getCredential();
        CredentialFactorySignatureValidator signatureValidator = new CredentialFactorySignatureValidator(new SigningCredentialFactory(
                entityId -> Collections.singletonList(signingCredential.getPublicKey())));

        Response eidasResponse = extractEidasResponseFromTranslator(translatorAppRule, buildSignedHubResponse(), URI.create(EIDAS_TEST_CONNECTOR_DESTINATION));

        Signature signature = eidasResponse.getSignature();

        assertThat(signature)
                .withFailMessage("EIDAS SAML Response needs to be signed")
                .isNotNull();

        assertThat(signatureValidator.validate(eidasResponse, null, Response.DEFAULT_ELEMENT_NAME))
                .withFailMessage("Invalid signature")
                .isTrue();

        assertThat(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1).isEqualTo(signature.getSignatureAlgorithm());
    }

    @Test
    public void shouldReturnAnEncryptedEidasResponse() throws Exception {
        Response eidasResponse = extractEidasResponseFromTranslator(translatorAppRule, buildSignedHubResponse(), URI.create(EIDAS_TEST_CONNECTOR_DESTINATION));

        assertThat(eidasResponse.getEncryptedAssertions()).hasSize(1);
        assertThat(eidasResponse.getAssertions()).isEmpty();
    }

    @Test
    public void shouldReturnAnEncryptedEidasResponseForECSigning() throws Exception {
        Response eidasResponse = extractEidasResponseFromTranslator(translatorAppRuleWithECSigning, buildSignedHubResponse(), URI.create(EIDAS_TEST_CONNECTOR_DESTINATION));
        assertThat(
                eidasResponse.getSignature().getSignatureAlgorithm())
                .isEqualTo(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA384);
    }

    @Test
    public void shouldRespondWithHTTP400ErrorForMalformedRequest() throws Exception {
        Response signedResponse = buildSignedHubResponse();
        javax.ws.rs.core.Response response = postMalformedHubResponseToTranslator(signedResponse);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void shouldDecryptAndReadEidasAssertion() throws Exception {
        Response hubResponse = buildSignedHubResponse();
        Response eidasResponse = extractEidasResponseFromTranslator(translatorAppRule, hubResponse, URI.create(EIDAS_TEST_CONNECTOR_DESTINATION));
        Response decryptedEidasResponse = decryptResponse(eidasResponse, eidasDecryptingCredential);

        assertThat(hubResponse.getInResponseTo()).isEqualTo(eidasResponse.getInResponseTo());

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(decryptedEidasResponse);
    }

    @Test
    public void eidasResponseShouldContainCorrectAttributes() throws Exception {
        Response decryptedEidasResponse =
                decryptResponse(
                        extractEidasResponseFromTranslator(translatorAppRule, buildSignedHubResponse(), URI.create(EIDAS_TEST_CONNECTOR_DESTINATION)),
                        eidasDecryptingCredential
                );

        TranslatedHubResponseTestAssertions.checkAllAttributesValid(decryptedEidasResponse);
    }

    @Test
    public void shouldLogEidasResponseAttributesToMDC() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);
        logger.addAppender(appender);

        Response eidasResponse = extractEidasResponseFromTranslator(translatorAppRule, buildSignedHubResponse(), URI.create(EIDAS_TEST_CONNECTOR_DESTINATION));

        verify(appender, Mockito.times(4)).doAppend(loggingEventArgumentCaptor.capture());

        final List<ILoggingEvent> logEvents = loggingEventArgumentCaptor.getAllValues();
        final Map<String, String> mdcPropertyMap = logEvents.stream()
                .filter(e -> MESSAGE_EGRESS.equals(e.getMessage()))
                .findFirst()
                .map(ILoggingEvent::getMDCPropertyMap)
                .orElseThrow();

        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.EIDAS_DESTINATION.name())).isEqualTo(EIDAS_TEST_CONNECTOR_DESTINATION);
        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name())).isEqualTo(ResponseBuilder.DEFAULT_REQUEST_ID);
        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.EIDAS_ISSUER.name())).isEqualTo(eidasResponse.getIssuer().getValue());

        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_INGRESS)).hasSize(1);
        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_EGRESS)).hasSize(1);
    }

    @Test
    public void failureResponseEndpointShouldReturnASamlFailureResponse() throws Exception {
        var failureResponseGenerationRequest = new SamlFailureResponseGenerationRequest(
                BAD_REQUEST, "this_a_badly_generated_saml_request", "http://destinationUrl", URI.create("http://entityId")
        );
        var failureResponse = translatorAppRule
                .target(Urls.TranslatorUrls.TRANSLATOR_ROOT + Urls.TranslatorUrls.GENERATE_FAILURE_RESPONSE_PATH)
                .request()
                .post(Entity.json(failureResponseGenerationRequest));
        Response response = new SamlParser().parseSamlString(Base64.decodeAsString(failureResponse.readEntity(String.class)));
        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo("urn:oasis:names:tc:SAML:2.0:status:Requester");
    }

    @Test
    public void shouldRespondWithDifferentEidasIssuerEntityIds() throws Exception {
        String firstEidasIssuerEntityId =
                extractEidasResponseFromTranslator(
                        translatorAppRule,
                        buildSignedHubResponse(),
                        URI.create(EIDAS_TEST_CONNECTOR_DESTINATION)
                ).getIssuer().getValue();

        buildAssertions();
        String secondEidasIssuerEntityId =
                extractEidasResponseFromTranslator(
                        translatorAppRule,
                        buildSignedHubResponse(),
                        URI.create(ALTERNATIVE_EIDAS_TEST_CONNECTOR_DESTINATION)
                ).getIssuer().getValue();

        assertThat(firstEidasIssuerEntityId).isNotEqualTo(secondEidasIssuerEntityId);
    }


    private Response extractEidasResponseFromTranslator(AppRule<TranslatorConfiguration> translatorAppRule, Response hubResponse, URI eidasIssuerEntityId) throws Exception {
        String translatorResponse = postHubResponseToTranslator(translatorAppRule, hubResponse, eidasIssuerEntityId).readEntity(String.class);
        return new SamlParser().parseSamlString(Base64.decodeAsString(translatorResponse));
    }

    private javax.ws.rs.core.Response postMalformedHubResponseToTranslator(Response hubResponse) throws Exception {
        String encodedResponse = Base64.encodeAsString("");

        HubResponseTranslatorRequest hubResponseTranslatorRequest =
            new HubResponseTranslatorRequest(
                encodedResponse,
                "_request-id_of-20-characters-or-more",
                ResponseBuilder.DEFAULT_REQUEST_ID,
                "LEVEL_2",
                URI.create(EIDAS_TEST_CONNECTOR_DESTINATION),
                URI.create(EIDAS_TEST_CONNECTOR_DESTINATION),
                false
            );

        return postToTranslator(translatorAppRule, hubResponseTranslatorRequest,  Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH);
    }

    private javax.ws.rs.core.Response postHubResponseToTranslator(AppRule<TranslatorConfiguration> translatorAppRule,
                                                                  Response hubResponse,
                                                                  URI eidasIssuerEntityId) throws Exception {
        String encodedResponse = Base64.encodeAsString(MARSHALLER.transformToString(hubResponse));

        HubResponseTranslatorRequest hubResponseTranslatorRequest =
                new HubResponseTranslatorRequest(
                        encodedResponse,
                        "_request-id_of-20-characters-or-more",
                        ResponseBuilder.DEFAULT_REQUEST_ID,
                        "LEVEL_2",
                        URI.create(EIDAS_TEST_CONNECTOR_DESTINATION),
                        eidasIssuerEntityId,
                        false
                );

        return postToTranslator(translatorAppRule, hubResponseTranslatorRequest,  Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH);
    }

    private javax.ws.rs.core.Response postToTranslator(AppRule<TranslatorConfiguration> translatorAppRule, HubResponseTranslatorRequest hubResponseTranslatorRequest, String response_path) throws URISyntaxException {
        return translatorAppRule
                .target(Urls.TranslatorUrls.TRANSLATOR_ROOT + response_path)
                .request()
                .post(Entity.json(hubResponseTranslatorRequest));
    }

    private static Response decryptResponse(Response response, Credential credential) throws Exception {
        ResponseAssertionDecrypter decrypter = new ResponseAssertionDecrypter(credential);
        return decrypter.decrypt(response);
    }

    private Response buildSignedHubResponse() throws MarshallingException, SignatureException {
        return getHubResponseBuilder().buildSigned(hubSigningCredential);
    }

    private Response buildSignedHubResponse(BasicCredential credential) throws MarshallingException, SignatureException {
        return getHubResponseBuilder().buildSigned(credential);
    }

    private HubResponseBuilder getHubResponseBuilder() {
        return new HubResponseBuilder()
                .withIssuer(TestEntityIds.HUB_ENTITY_ID)
                .withDestination(EIDAS_TEST_CONNECTOR_DESTINATION)
                .addEncryptedAssertion(authnAssertion)
                .addEncryptedAssertion(matchingDatasetAssertion);
    }
}
