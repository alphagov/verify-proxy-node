package uk.gov.ida.notification.saml;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.logging.LoggingUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SamlObjectSignerTest extends SamlInitializedTest {

    private static final TestKeyPair TEST_KEY_PAIR;
    private static final BasicX509Credential SIGNING_CREDENTIAL;
    private static final SamlObjectSigner SAML_OBJECT_SIGNER;

    static {
        try {
            TEST_KEY_PAIR = new TestKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SIGNING_CREDENTIAL = TEST_KEY_PAIR.getX509Credential();
        SAML_OBJECT_SIGNER = new SamlObjectSigner(SIGNING_CREDENTIAL, SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, 99L);
    }

    @Test
    public void shouldSignAuthRequest() throws Throwable {
        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);
        SAML_OBJECT_SIGNER.sign(authnRequest, "response-id");
        Signature signature = authnRequest.getSignature();

        String actualCertificate = signature.getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue();

        assertThat(TEST_KEY_PAIR.getEncodedCertificate()).isEqualTo(actualCertificate.replaceAll("\\s+", ""));
        assertThat(signature).isNotNull();
        String algoIdSignatureRsaSha256 = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        assertThat(signature.getSignatureAlgorithm()).isEqualTo(algoIdSignatureRsaSha256);
        assertThat(signature.getSigningCredential().getPublicKey()).isEqualTo(SIGNING_CREDENTIAL.getPublicKey());
        assertThat(signature.getSigningCredential().getPrivateKey()).isEqualTo(SIGNING_CREDENTIAL.getPrivateKey());
        assertThat(signature.getCanonicalizationAlgorithm()).isEqualTo(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        SignatureValidator.validate(signature, SIGNING_CREDENTIAL);
    }

    @Test
    public void shouldLogKeyHandleWhenKeyHandleIsNotNull() throws Exception {
        LoggingUtil.hijackJDKLogging();

        Appender<ILoggingEvent> appender = mock(Appender.class);
        Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);
        logger.addAppender(appender);

        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);
        SAML_OBJECT_SIGNER.sign(authnRequest, "response-id");

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(1)).doAppend(loggingEventArgumentCaptor.capture());

        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();

        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.HSM_KEY_HANDLE.name())).isEqualTo("99");
    }

    @Test
    public void shouldNotLogKeyHandleWhenKeyHandleIsNull() throws Exception {
        final SamlObjectSigner samlObjectSigner = new SamlObjectSigner(SIGNING_CREDENTIAL, SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, null);
        LoggingUtil.hijackJDKLogging();

        Appender<ILoggingEvent> appender = mock(Appender.class);
        Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);
        logger.addAppender(appender);

        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);
        samlObjectSigner.sign(authnRequest, "response-id");

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(1)).doAppend(loggingEventArgumentCaptor.capture());

        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();

        assertThat(mdcPropertyMap).doesNotContainKey(ProxyNodeMDCKey.HSM_KEY_HANDLE.name());
    }
}
