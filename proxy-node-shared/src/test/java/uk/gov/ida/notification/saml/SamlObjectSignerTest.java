package uk.gov.ida.notification.saml;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.logging.LoggingUtil;
import org.junit.Before;
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
    private TestKeyPair testKeyPair;
    private SamlObjectSigner samlObjectSigner;
    private BasicX509Credential signingCredential;

    @Before
    public void setup() throws Throwable {
        testKeyPair = new TestKeyPair();
        signingCredential = testKeyPair.getX509Credential();
        samlObjectSigner = new SamlObjectSigner(signingCredential, SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, 99L);
    }

    @Test
    public void shouldSignAuthRequest() throws Throwable {
        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);
        samlObjectSigner.sign(authnRequest, "response-id");
        Signature signature = authnRequest.getSignature();

        String actualCertificate = signature.getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue();

        assertThat(testKeyPair.getEncodedCertificate()).isEqualTo(actualCertificate.replaceAll("\\s+", ""));
        assertThat(signature).isNotNull();
        String algoIdSignatureRsaSha256 = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        assertThat(signature.getSignatureAlgorithm()).isEqualTo(algoIdSignatureRsaSha256);
        assertThat(signature.getSigningCredential().getPublicKey()).isEqualTo(signingCredential.getPublicKey());
        assertThat(signature.getSigningCredential().getPrivateKey()).isEqualTo(signingCredential.getPrivateKey());
        assertThat(signature.getCanonicalizationAlgorithm()).isEqualTo(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        SignatureValidator.validate(signature, signingCredential);
    }

    @Test
    public void shouldLogKeyHandleWhenKeyHandleIsNotNull() throws Exception {
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

        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.HSM_KEY_HANDLE.name())).isEqualTo("99");
    }

    @Test
    public void shouldNotLogKeyHandleWhenKeyHandleIsNull() throws Exception {
        samlObjectSigner = new SamlObjectSigner(signingCredential, SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, null);
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
