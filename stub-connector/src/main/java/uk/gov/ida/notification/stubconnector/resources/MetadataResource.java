package uk.gov.ida.notification.stubconnector.resources;

import org.joda.time.DateTime;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.KeyValue;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.w3c.dom.Document;
import se.litsec.opensaml.utils.SignatureUtils;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.saml.SamlBuilder;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

@Path("/Metadata")
@Produces("application/samlmetadata+xml")
public class MetadataResource {
    private final Document metadata;
    private final X509CertificateFactory x509CertificateFactory;
    private final X509Credential signingCredential;

    public MetadataResource(StubConnectorConfiguration connectorConfiguration, X509Credential signingCredential) throws MarshallingException, SecurityException, SignatureException {
        this.signingCredential = signingCredential;
        this.x509CertificateFactory = new X509CertificateFactory();
        this.metadata = new SamlObjectMarshaller().marshallToElement(buildEntityDescriptor(connectorConfiguration)).getOwnerDocument();
    }

    @GET
    public Document connectorMetadata() {
        return metadata;
    }

    private EntityDescriptor buildEntityDescriptor(StubConnectorConfiguration connectorConfiguration) throws SecurityException, SignatureException {
        AssertionConsumerService assertionConsumerService = SamlBuilder.build(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        assertionConsumerService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        assertionConsumerService.setLocation(connectorConfiguration.getConnectorNodeBaseUrl() + "/SAML2/Response/POST");
        assertionConsumerService.setIndex(1);
        assertionConsumerService.setIsDefault(true);

        SPSSODescriptor spSsoDescriptor = SamlBuilder.build(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        spSsoDescriptor.addSupportedProtocol(SAMLConstants.SAML20_NS);
        spSsoDescriptor.getAssertionConsumerServices().add(assertionConsumerService);
        spSsoDescriptor.getKeyDescriptors().add(buildKeyDescriptor(connectorConfiguration.getSigningKeyPair(), UsageType.SIGNING));
        spSsoDescriptor.getKeyDescriptors().add(buildKeyDescriptor(connectorConfiguration.getEncryptionKeyPair(), UsageType.ENCRYPTION));

        EntityDescriptor entityDescriptor = SamlBuilder.build(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        entityDescriptor.setEntityID(connectorConfiguration.getConnectorNodeBaseUrl() + "/Metadata");
        entityDescriptor.setValidUntil(DateTime.now().plusDays(1));
        entityDescriptor.getRoleDescriptors().add(spSsoDescriptor);

        SignatureUtils.sign(entityDescriptor, signingCredential);

        return entityDescriptor;
    }

    private KeyDescriptor buildKeyDescriptor(KeyPairConfiguration signingKeyPair, UsageType usageType) throws SecurityException {
        X509Certificate signingCert = x509CertificateFactory.createCertificate(
                signingKeyPair.getPublicKey().getCert()
        );

        KeyDescriptor keyDescriptor = SamlBuilder.build(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        keyDescriptor.setUse(usageType);
        keyDescriptor.setKeyInfo(buildKeyInfo(new BasicX509Credential(signingCert)));

        return keyDescriptor;
    }

    private KeyInfo buildKeyInfo(Credential credential) throws SecurityException {
        X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(credential);

        KeyValue keyValue = SamlBuilder.build(KeyValue.DEFAULT_ELEMENT_NAME);
        RSAPublicKey publicKey = (RSAPublicKey) credential.getPublicKey();
        keyValue.setRSAKeyValue(KeyInfoSupport.buildRSAKeyValue(publicKey));

        keyInfo.getKeyValues().add(keyValue);

        return keyInfo;
    }
}
