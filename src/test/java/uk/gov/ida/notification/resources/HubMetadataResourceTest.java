package uk.gov.ida.notification.resources;

import org.bouncycastle.util.encoders.Base64;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.X509Certificate;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.security.cert.Certificate;

import static org.junit.Assert.assertEquals;
import static uk.gov.ida.notification.helpers.PKIHelpers.parseCert;
import static uk.gov.ida.notification.helpers.PKIHelpers.getCertificateFromFile;

public class HubMetadataResourceTest {
    private final HubMetadataResource hubMetadataResource = new HubMetadataResource();
    private EntitiesDescriptor hubMetadata;

    @Before
    public void setUp() throws Exception {
        InitializationService.initialize();
        Response hubMetadataResponse = hubMetadataResource.getHubMetadata("local");
        String samlObject = hubMetadataResponse.getEntity().toString();
        hubMetadata = new SamlParser().parseSamlString(samlObject);
    }

    @Test
    public void shouldReturnSigningCertInMetadata() throws Exception {
        String hubSigningCertFile = "local/hub_signing_primary.crt";
        Certificate expectedCertificate = getCertificateFromFile(hubSigningCertFile);

        SPSSODescriptor spssoDescriptor = hubMetadata.getEntityDescriptors().get(0).getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        X509Certificate hubSigningCert = getCertFromSSODescriptor(spssoDescriptor, UsageType.SIGNING);

        Certificate actualCertificate = parseMetadataCert(hubSigningCert.getValue());

        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    public void shouldReturnEncryptionCertInMetadata() throws Exception {
        String hubEncryptionCertFile = "local/hub_encryption_primary.crt";
        Certificate expectedCertificate = getCertificateFromFile(hubEncryptionCertFile);

        SPSSODescriptor spSsoDescriptor = hubMetadata.getEntityDescriptors().get(0).getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        X509Certificate hubEncryptionCert = getCertFromSSODescriptor(spSsoDescriptor, UsageType.ENCRYPTION);

        Certificate actualCertificate = parseMetadataCert(hubEncryptionCert.getValue());

        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    public void shouldReturnStubIDPSigningCertInMetadata() throws Exception {
        String idpSigningCertFile = "local/stub_idp_signing_primary.crt";
        Certificate expectedCertificate = getCertificateFromFile(idpSigningCertFile);

        IDPSSODescriptor idpSsoDescriptor = hubMetadata.getEntityDescriptors().get(1).getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
        X509Certificate idpSigningCert = getCertFromSSODescriptor(idpSsoDescriptor, UsageType.SIGNING);

        Certificate actualCertificate = parseMetadataCert(idpSigningCert.getValue());

        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    public void should404WhenInvalidEnvironmentProvided() {
        Response response = hubMetadataResource.getHubMetadata("missing");
        assertEquals(response.getStatusInfo(), Status.NOT_FOUND);
    }

    private X509Certificate getCertFromSSODescriptor(SSODescriptor spssoDescriptor, UsageType usageType) throws Exception {
        KeyDescriptor signingKeyDescriptor = spssoDescriptor.getKeyDescriptors()
                .stream()
                .filter((x) -> x.getUse().equals(usageType))
                .findFirst()
                .orElseThrow(Exception::new);
        return signingKeyDescriptor.getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0);
    }

    private Certificate parseMetadataCert(String certString) throws Exception {
        String trimmedCertString = certString.replaceAll("\\n", "").replaceAll("\\s", "");
        byte[] decoded = Base64.decode(trimmedCertString);
        return parseCert(decoded);
    }
}
