package uk.gov.ida.eidas.metatron.apprule.rules;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.AssertionConsumerServiceBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.SPSSODescriptorBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class TestCountryMetadataResource {
    public static final String VALID_ONE = "valid-one";
    public static final String VALID_TWO = "valid-two";
    public static final String EXPIRED = "expired";
    public static final String UNSIGNED = "unsigned";
    public static final String UNTRUSTED = "untrusted";
    public static final String DISABLED = "disabled";
    public static final String THROWS_ERROR = "error";
    public static final String DOES_NOT_EXIST = "does-not-exist";

    private Map<String, String> connectorMetadatas;

    @GET
    @Path("/{country}/Metadata")
    public Response metadata(@PathParam("country") String country) {
        if (country.equals(THROWS_ERROR)) {
            return Response.serverError().build();
        }

        return Response.ok(connectorMetadatas.get(country)).build();
    }

    void initialiseCountryMetadatas(int port) throws Exception {
        final MetadataFactory metadataFactory = new MetadataFactory();

        connectorMetadatas = new HashMap<>();
        connectorMetadatas.put(VALID_ONE, metadataFactory.singleEntityMetadata(buildConnectorEntityDescriptor(buildEntityId(port, VALID_ONE))));
        connectorMetadatas.put(VALID_TWO, metadataFactory.singleEntityMetadata(buildConnectorEntityDescriptor(buildEntityId(port, VALID_TWO))));
        connectorMetadatas.put(EXPIRED, metadataFactory.singleEntityMetadata(buildExpiredEntityDescriptor(buildEntityId(port, EXPIRED))));
        connectorMetadatas.put(UNSIGNED, metadataFactory.singleEntityMetadata(buildUnsignedConnectorEntityDescriptor(buildEntityId(port, UNSIGNED))));
        connectorMetadatas.put(UNTRUSTED, metadataFactory.singleEntityMetadata(buildConnectorEntityDescriptor(buildEntityId(port, UNTRUSTED))));
        connectorMetadatas.put(DISABLED, metadataFactory.singleEntityMetadata(buildConnectorEntityDescriptor(buildEntityId(port, DISABLED))));
        connectorMetadatas.put(THROWS_ERROR, metadataFactory.singleEntityMetadata(buildConnectorEntityDescriptor(buildEntityId(port, THROWS_ERROR))));
    }

    private static String buildEntityId(int port, String name) {
        return "http://localhost:" + port + "/application/" + name + "/Metadata";
    }

    private static EntityDescriptor buildConnectorEntityDescriptor(String entityId) throws Exception {
        return EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(entityId)
                .withIdpSsoDescriptor(null)
                .setAddDefaultSpServiceDescriptor(false)
                .addSpServiceDescriptor(getSpssoDescriptor(entityId))
                .withValidUntil(DateTime.now().plusWeeks(2))
                .withSignature(getSignature())
                .build();
    }

    private static EntityDescriptor buildExpiredEntityDescriptor(String entityId) throws Exception {
        return EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(entityId)
                .withIdpSsoDescriptor(null)
                .setAddDefaultSpServiceDescriptor(false)
                .addSpServiceDescriptor(getSpssoDescriptor(entityId))
                .withValidUntil(DateTime.now().minusWeeks(2))
                .withSignature(getSignature())
                .build();
    }

    private static EntityDescriptor buildUnsignedConnectorEntityDescriptor(String entityId) throws Exception {
        return EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(entityId)
                .withIdpSsoDescriptor(null)
                .setAddDefaultSpServiceDescriptor(false)
                .addSpServiceDescriptor(getSpssoDescriptor(entityId))
                .withValidUntil(DateTime.now().plusWeeks(2))
                .withSignature(null)
                .build();
    }

    private static SPSSODescriptor getSpssoDescriptor(String entityID) {
        KeyDescriptor encryptionKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                .withX509ForEncryption(uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT)
                .build();

        KeyDescriptor signingKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                .withX509ForSigning(uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT)
                .build();

        final SPSSODescriptorBuilder spssoDescriptorBuilder = SPSSODescriptorBuilder.anSpServiceDescriptor()
                .withoutDefaultSigningKey()
                .withoutDefaultEncryptionKey()
                .addKeyDescriptor(signingKeyDescriptor)
                .addKeyDescriptor(encryptionKeyDescriptor)
                .addAssertionConsumerService(AssertionConsumerServiceBuilder.anAssertionConsumerService().build());

        if (entityID.contains(VALID_TWO)) {
            spssoDescriptorBuilder.addAssertionConsumerService(
                    AssertionConsumerServiceBuilder
                            .anAssertionConsumerService()
                            .withLocation("http://foo.com/bar2")
                            .withIndex(0)
                            .isDefault()
                            .build());
        }

        return spssoDescriptorBuilder.build();
    }

    private static Signature getSignature() {
        return SignatureBuilder.aSignature()
                .withSigningCredential(new TestCredentialFactory(uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT, uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
                .withX509Data(uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT)
                .build();
    }
}
