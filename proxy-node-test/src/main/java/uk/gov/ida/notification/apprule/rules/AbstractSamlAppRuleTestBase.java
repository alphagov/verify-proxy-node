package uk.gov.ida.notification.apprule.rules;

import com.google.common.base.Stopwatch;
import io.dropwizard.testing.junit.DropwizardClientRule;
import keystore.KeyStoreResource;
import org.opensaml.core.config.InitializationService;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.helpers.X509CredentialFactory;
import uk.gov.ida.notification.saml.SamlObjectSigner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public abstract class AbstractSamlAppRuleTestBase {

    private static final long CONNECTOR_METADATA_TIMEOUT = 60;

    private static TestMetadataResource testMetadataResource;

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected static DropwizardClientRule createInitialisedClientRule(Object resource) {
        try {
            return new DropwizardClientRule(resource) {{
                this.before();
            }};
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    protected static DropwizardClientRule createTestMetadataClientRule() {
        return createInitialisedClientRule(getTestMetadataResource());
    }

    protected static KeyStoreResource createMetadataTruststore() {
        final KeyStoreResource metadataTruststore = aKeyStoreResource()
                .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
                .build();

        metadataTruststore.create();
        return metadataTruststore;
    }

    protected static SamlObjectSigner createSamlObjectSigner() {
        try {
            return new SamlObjectSigner(X509CredentialFactory.build(
                    TEST_RP_PUBLIC_SIGNING_CERT, TEST_RP_PRIVATE_SIGNING_KEY), SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void waitForMetadata(String metadataUrl) {
        final URL metadataUrlToFetch;
        try {
            metadataUrlToFetch = new URL(metadataUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        final Stopwatch sw = Stopwatch.createStarted();
        String connectorMetadata = "";
        do {
            try {
                connectorMetadata = new Scanner(metadataUrlToFetch.openStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next();
            } catch (IOException ignored) {
            }
        } while (sw.elapsed().getSeconds() < CONNECTOR_METADATA_TIMEOUT && !connectorMetadata.contains("signing"));

        if (connectorMetadata.equals("")) {
            throw new RuntimeException("Couldn't fetch metadata from " + metadataUrl);
        }
    }

    private static TestMetadataResource getTestMetadataResource() {
        if (testMetadataResource == null) {
            try {
                return new TestMetadataResource();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return testMetadataResource;
    }
}
