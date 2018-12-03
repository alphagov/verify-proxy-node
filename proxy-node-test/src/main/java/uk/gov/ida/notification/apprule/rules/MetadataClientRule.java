package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.IdpSsoDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.SPSSODescriptorBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;

public class MetadataClientRule extends DropwizardClientRule {
    public MetadataClientRule() throws Exception {
        super(new TestMetadataResource());
    }

    @Path("/")
    public static class TestMetadataResource {
        private final String connectorEntityId = "http://connector-node:8080/ConnectorResponderMetadata";
        private String connectorMetadataXml;
        private String stubConnectorMetadataXml;
        private String hubMetadata;

        TestMetadataResource() throws Exception {
            connectorMetadataXml = new MetadataFactory().singleEntityMetadata(buildConnectorEntityDescriptor());
            stubConnectorMetadataXml = new MetadataFactory().singleEntityMetadata(buildStubConnectorEntityDescriptor());
            hubMetadata = new MetadataFactory().defaultMetadata();
        }

        private EntityDescriptor buildConnectorEntityDescriptor() throws Exception {
            KeyDescriptor encryptionKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                    .withX509ForEncryption(TEST_RP_PUBLIC_ENCRYPTION_CERT)
                    .build();

            KeyDescriptor signingKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                    .withX509ForSigning(TEST_RP_PUBLIC_SIGNING_CERT)
                    .build();

            SPSSODescriptor spssoDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor()
                    .withoutDefaultSigningKey()
                    .withoutDefaultEncryptionKey()
                    .addKeyDescriptor(signingKeyDescriptor)
                    .addKeyDescriptor(encryptionKeyDescriptor)
                    .build();

            Signature signature = SignatureBuilder.aSignature()
                    .withSigningCredential(new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
                    .withX509Data(METADATA_SIGNING_A_PUBLIC_CERT)
                    .build();

            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(connectorEntityId)
                    .withIdpSsoDescriptor(null)
                    .setAddDefaultSpServiceDescriptor(false)
                    .addSpServiceDescriptor(spssoDescriptor)
                    .withValidUntil(DateTime.now().plusWeeks(2))
                    .withSignature(signature)
                    .setAddDefaultSpServiceDescriptor(false)
                    .build();
        }

        private EntityDescriptor buildStubConnectorEntityDescriptor() throws Exception {
            KeyDescriptor signingKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                    .withX509ForSigning(TEST_RP_PUBLIC_SIGNING_CERT)
                    .build();

            IDPSSODescriptor idpssoDescriptor = IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
                    .addKeyDescriptor(signingKeyDescriptor)
                    .build();


            Signature signature = SignatureBuilder.aSignature()
                    .withSigningCredential(new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
                    .withX509Data(METADATA_SIGNING_A_PUBLIC_CERT)
                    .build();

            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(connectorEntityId)
                    .withIdpSsoDescriptor(idpssoDescriptor)
                    .setAddDefaultSpServiceDescriptor(false)
                    .addSpServiceDescriptor(null)
                    .withValidUntil(DateTime.now().plusWeeks(2))
                    .withSignature(signature)
                    .setAddDefaultSpServiceDescriptor(false)
                    .build();
        }

        @GET
        @Path("/stub-connector/metadata")
        public String getStubConnectorMetadata() {
            return stubConnectorMetadataXml;
        }

        @GET
        @Path("/connector-node/metadata")
        public String getConnectorMetadata() {
            return connectorMetadataXml;
        }

        @GET
        @Path("/hub/metadata")
        public String getHubMetadata() {
            return hubMetadata;
        }
    }
}
