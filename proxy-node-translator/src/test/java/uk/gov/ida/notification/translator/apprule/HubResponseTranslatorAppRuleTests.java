package uk.gov.ida.notification.translator.apprule;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.assertj.core.api.Assertions;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.LoggerFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseTestAssertions;
import uk.gov.ida.notification.helpers.BasicCredentialBuilder;
import uk.gov.ida.notification.helpers.HubAssertionBuilder;
import uk.gov.ida.notification.helpers.HubResponseBuilder;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.translator.apprule.base.TranslatorAppRuleTestBase;
import uk.gov.ida.notification.translator.logging.HubResponseTranslatorLoggerHelper;
import uk.gov.ida.notification.translator.logging.HubResponseTranslatorLoggerHelper.HubResponseTranslatorLoggerAttributes;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;
import uk.gov.ida.saml.security.CredentialFactorySignatureValidator;
import uk.gov.ida.saml.security.SigningCredentialFactory;

import javax.ws.rs.client.Entity;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;

public class HubResponseTranslatorAppRuleTests extends TranslatorAppRuleTestBase {

    private static final String PROXY_NODE_ENTITY_ID = "http://proxy-node.uk";
    private static final String EIDAS_TEST_CONNECTOR_DESTINATION = "http://proxy-node/SAML2/SSO/Response";
    private static final SamlObjectMarshaller MARSHALLER = new SamlObjectMarshaller();
    private static final X509CertificateFactory X_509_CERTIFICATE_FACTORY = new X509CertificateFactory();

    private BasicCredential hubSigningCredential;
    private EncryptedAssertion authnAssertion;
    private EncryptedAssertion matchingDatasetAssertion;
    private Credential eidasDecryptingCredential;

    private static final String publicBuild = (System.getenv("VERIFY_USE_PUBLIC_BINARIES"));

    @Before
    public void setup() throws Throwable {

        Credential hubAssertionsEncryptionCredential = new BasicCredential(
                X_509_CERTIFICATE_FACTORY.createCertificate(HUB_TEST_PUBLIC_ENCRYPTION_CERT).getPublicKey()
        );

        eidasDecryptingCredential =
                new TestCredentialFactory(
                        STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                        STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                ).getDecryptingCredential();

        hubSigningCredential = BasicCredentialBuilder.instance()
                .withPublicSigningCert(HUB_TEST_PUBLIC_SIGNING_CERT)
                .withPrivateSigningKey(HUB_TEST_PRIVATE_SIGNING_KEY)
                .build();

        final BasicCredential idpSigningCredential = BasicCredentialBuilder.instance()
                .withPublicSigningCert(STUB_IDP_PUBLIC_PRIMARY_CERT)
                .withPrivateSigningKey(STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY)
                .build();

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
        Credential signingCredential = translatorAppRule.getConfiguration().getSignerConfiguration().getSigner().getCredential();
        CredentialFactorySignatureValidator signatureValidator = new CredentialFactorySignatureValidator(new SigningCredentialFactory(
                entityId -> Collections.singletonList(signingCredential.getPublicKey())));

        Response eidasResponse = extractEidasResponseFromTranslator(buildSignedHubResponse());

        Signature signature = eidasResponse.getSignature();

        assertThat(signature)
                .withFailMessage("EIDAS SAML Response needs to be signed")
                .isNotNull();

        assertThat(signatureValidator.validate(eidasResponse, null, Response.DEFAULT_ELEMENT_NAME))
                .withFailMessage("Invalid signature")
                .isTrue();

        assertThat(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256).isEqualTo(signature.getSignatureAlgorithm());
    }

    @Test
    public void shouldReturnAnEncryptedEidasResponse() throws Exception {
        Response eidasResponse = extractEidasResponseFromTranslator(buildSignedHubResponse());
        assertThat(1).isEqualTo(eidasResponse.getEncryptedAssertions().size());
        assertThat(eidasResponse.getAssertions()).isEmpty();
    }

    @Test
    public void shouldDecryptAndReadEidasAssertion() throws Exception {

        Response hubResponse = buildSignedHubResponse();
        Response eidasResponse = extractEidasResponseFromTranslator(hubResponse);
        Response decryptedEidasResponse = decryptResponse(eidasResponse, eidasDecryptingCredential);

        assertThat(hubResponse.getInResponseTo()).isEqualTo(eidasResponse.getInResponseTo());

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(decryptedEidasResponse);
    }

    @Test
    public void eidasResponseShouldContainCorrectAttributes() throws Exception {

        Response decryptedEidasResponse =
                decryptResponse(
                        extractEidasResponseFromTranslator(buildSignedHubResponse()),
                        eidasDecryptingCredential
                );

        TranslatedHubResponseTestAssertions.assertAttributes(decryptedEidasResponse);
    }

    @Test
    public void shouldLogEidasResponseAttributesToMDC() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(HubResponseTranslatorLoggerHelper.class);
        Appender<ILoggingEvent> appender = mock(Appender.class);
        logger.addAppender(appender);

        Response eidasResponse = extractEidasResponseFromTranslator(buildSignedHubResponse());

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);

        // Resource method logs twice: once for Response Details and once for the Hash, so expect 2 invocations.
        verify(appender, Mockito.times(2)).doAppend(loggingEventArgumentCaptor.capture());

        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();

        Assertions.assertThat(loggingEvent.getMessage()).contains(HubResponseTranslatorLoggerHelper.EIDAS_RESPONSE_LOGGER_MESSAGE);
        Assertions.assertThat(mdcPropertyMap.get(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ID)).isEqualTo(eidasResponse.getID());
        Assertions.assertThat(mdcPropertyMap.get(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_DESTINATION)).isEqualTo(EIDAS_TEST_CONNECTOR_DESTINATION);
        Assertions.assertThat(mdcPropertyMap.get(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_IN_RESPONSE_TO)).isEqualTo(ResponseBuilder.DEFAULT_REQUEST_ID);
        Assertions.assertThat(mdcPropertyMap.get(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ISSUER)).isEqualTo(eidasResponse.getIssuer().getValue());
    }

    @Test
    public void shouldLogEidasResponseAttributesToConsole() throws Exception {

        ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
        PrintStream defaultConsolePrintStream = System.out;
        System.setOut(new PrintStream(consoleOutput));

        Response eidasResponse = extractEidasResponseFromTranslator(buildSignedHubResponse());

        // Checking System.out does not work in a Travis build.  Only run this assertion outside of Travis.
        if (!"true".equals(publicBuild)) {
            // Proxy Node logs in json format
            Assertions.assertThat(consoleOutput.toString()).containsPattern(
                    Pattern.compile(
                            String.format("%s.*\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"",
                                    HubResponseTranslatorLoggerHelper.EIDAS_RESPONSE_LOGGER_MESSAGE,
                                    HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_DESTINATION, EIDAS_TEST_CONNECTOR_DESTINATION,
                                    HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ID, eidasResponse.getID(),
                                    HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_IN_RESPONSE_TO, ResponseBuilder.DEFAULT_REQUEST_ID,
                                    HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ISSUER, eidasResponse.getIssuer().getValue()
                            )
                    )
            );
        }
        System.setOut(defaultConsolePrintStream);
    }

    private Response extractEidasResponseFromTranslator(Response hubResponse) throws Exception {
        return new SamlParser().parseSamlString(Base64.decodeAsString(postHubResponseToTranslator(hubResponse).readEntity(String.class)));
    }

    private javax.ws.rs.core.Response postHubResponseToTranslator(Response hubResponse) throws URISyntaxException {
        String encodedResponse = Base64.encodeAsString(MARSHALLER.transformToString(hubResponse));

        HubResponseTranslatorRequest hubResponseTranslatorRequest =
                new HubResponseTranslatorRequest(
                        encodedResponse,
                        "_1234",
                        ResponseBuilder.DEFAULT_REQUEST_ID,
                        "LEVEL_2",
                        URI.create(EIDAS_TEST_CONNECTOR_DESTINATION),
                        STUB_COUNTRY_PUBLIC_PRIMARY_CERT
                );

        return translatorAppRule
                .target(Urls.TranslatorUrls.TRANSLATOR_ROOT + Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH)
                .request()
                .post(Entity.json(hubResponseTranslatorRequest));
    }

    private static Response decryptResponse(Response response, Credential credential) {
        ResponseAssertionDecrypter decrypter = new ResponseAssertionDecrypter(credential);
        return decrypter.decrypt(response);
    }

    private Response buildSignedHubResponse() throws MarshallingException, SignatureException {
        return getHubResponseBuilder().buildSigned(hubSigningCredential);
    }

    private HubResponseBuilder getHubResponseBuilder() {
        return new HubResponseBuilder()
                .withIssuer(TestEntityIds.HUB_ENTITY_ID)
                .withDestination(EIDAS_TEST_CONNECTOR_DESTINATION)
                .addEncryptedAssertion(authnAssertion)
                .addEncryptedAssertion(matchingDatasetAssertion);
    }
}
