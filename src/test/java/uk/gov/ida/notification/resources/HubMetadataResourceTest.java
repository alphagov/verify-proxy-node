package uk.gov.ida.notification.resources;

import com.google.common.io.Resources;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.X509Certificate;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import static org.junit.Assert.assertEquals;

public class HubMetadataResourceTest {

    private EntitiesDescriptor hubMetadata;

    @Before
    public void setUp() throws Exception {
        InitializationService.initialize();
        HubMetadataResource hubMetadataResource = new HubMetadataResource();
        Response hubMetadaResponse = hubMetadataResource.getHubMetadata();
        String samlObject = hubMetadaResponse.getEntity().toString();
        hubMetadata = (EntitiesDescriptor) new SamlParser().parseSamlString(samlObject);
    }

    @Test
    public void shouldReturnSigningCertInMetadata() throws Exception {
        Certificate expectedCertificate = parseCert(Resources.toByteArray(Resources.getResource("pki/hub_signing.crt")));

        SPSSODescriptor spssoDescriptor = hubMetadata.getEntityDescriptors().get(0).getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        X509Certificate hubSigningCert = getCertFromSPSSODescriptor(spssoDescriptor, UsageType.SIGNING);

        Certificate actualCertificate = parseMetadataCert(hubSigningCert.getValue());

        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    public void shouldReturnEncryptionCertInMetadata() throws Exception {
        Certificate expectedCertificate = parseCert(Resources.toByteArray(Resources.getResource("pki/hub_encryption.crt")));

        SPSSODescriptor spssoDescriptor = hubMetadata.getEntityDescriptors().get(0).getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        X509Certificate hubEncryptionCert = getCertFromSPSSODescriptor(spssoDescriptor, UsageType.ENCRYPTION);

        Certificate actualCertificate = parseMetadataCert(hubEncryptionCert.getValue());

        assertEquals(expectedCertificate, actualCertificate);
    }

    private X509Certificate getCertFromSPSSODescriptor(SPSSODescriptor spssoDescriptor, UsageType usageType) throws Exception {
        KeyDescriptor signingKeyDescriptor = spssoDescriptor.getKeyDescriptors()
                .stream()
                .filter((x) -> x.getUse().equals(usageType))
                .findFirst()
                .orElseThrow(Exception::new);
        return signingKeyDescriptor.getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0);
    }

    private Certificate parseCert(byte[] certificateBytes) throws Exception {
        ByteArrayInputStream certificateStream = new ByteArrayInputStream(certificateBytes);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return certificateFactory.generateCertificate(certificateStream);
    }

    private Certificate parseMetadataCert(String certString) throws Exception {
        String trimmedCertString = certString.replaceAll("\\n", "").replaceAll("\\s", "");
        byte[] decoded = Base64.decode(trimmedCertString);
        return parseCert(decoded);
    }
}
