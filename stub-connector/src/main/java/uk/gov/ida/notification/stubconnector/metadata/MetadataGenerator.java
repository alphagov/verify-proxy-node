package uk.gov.ida.notification.stubconnector.metadata;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import se.litsec.opensaml.utils.ObjectUtils;
import software.amazon.awssdk.core.SdkBytes;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MetadataGenerator {

    public static final String CONNECTOR_TEMPLATE_XML_MUSTACHE = "connector_template.xml.mustache";
    public static final String TEMPLATE_KEY_ENTITY_ID = "entity_id";
    private final X509KeyInfoGeneratorFactory keyInfoGeneratorFactory;
    private final StubConnectorConfiguration configuration;

    public MetadataGenerator(StubConnectorConfiguration configuration) {
        this.configuration = configuration;
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException(e);
        }
        keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
    }

    private String renderTemplate(String template, Map values) {
        Mustache mustache = new DefaultMustacheFactory().compile(template);
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, values);
        stringWriter.flush();
        return stringWriter.toString();
    }

    public EntityDescriptor getConnectorMetadata() throws Exception {
        Map<String, String> config = configuration.getConnectorNodeTemplateConfig();
        config.put(TEMPLATE_KEY_ENTITY_ID, configuration.getConnectorNodeEntityId().toString());
        String xml = renderTemplate(CONNECTOR_TEMPLATE_XML_MUSTACHE, config);
        EntityDescriptor entityDescriptor = ObjectUtils.unmarshall(new ByteArrayInputStream(xml.getBytes()), EntityDescriptor.class);
        entityDescriptor.setID("_" + UUID.randomUUID().toString());
        entityDescriptor.setValidUntil(DateTime.now().plusWeeks(2));
        updateSsoDescriptors(entityDescriptor);
        sign(entityDescriptor, xml);
        return entityDescriptor;
    }

    private void sign(EntityDescriptor entityDescriptor, String xml) throws SecurityException, MarshallingException, SignatureException {
        KmsClient client = KmsClient.create();
        SignRequest signRequest = SignRequest.builder()
                .messageType("RAW")
                .keyId("dcdc859f-78f5-4752-a6aa-feba894d5d37")
                .message(
                        SdkBytes.fromInputStream(new ByteArrayInputStream(xml.getBytes()))
                )
                .signingAlgorithm("RSASSA_PSS_SHA_256")
                .build();
        SignResponse sign = client.sign(signRequest);
        client.close();

        SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setXMLSignature();
        entityDescriptor.setSignature(new Signature());

        SignatureSigningParameters signingParams = new SignatureSigningParameters();
        signingParams.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
        signingParams.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signingParams.setSigningCredential(configuration.getCredentialConfiguration().getMetadataSigningCredential());
        signingParams.setKeyInfoGenerator(keyInfoGeneratorFactory.newInstance());
        SignatureSupport.signObject(entityDescriptor, signingParams);
        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(Optional.ofNullable(entityDescriptor.getSignature()).orElseThrow(() -> new RuntimeException("Signature missing")));
        SignatureValidator.validate(entityDescriptor.getSignature(), configuration.getCredentialConfiguration().getMetadataSigningCredential());
    }

    private SSODescriptor getSsoDescriptor(EntityDescriptor entityDescriptor) {
        return entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
    }

    private void updateSsoDescriptors(EntityDescriptor entityDescriptor) throws Exception {
        SSODescriptor spSso = getSsoDescriptor(entityDescriptor);
        addSamlSigningKeyDescriptor(spSso);
        addSamlEncryptionDescriptor(spSso);
    }

    private void addSamlSigningKeyDescriptor(SSODescriptor spSso) throws Exception {
        spSso.getKeyDescriptors().add(buildKeyDescriptor(UsageType.SIGNING, configuration.getCredentialConfiguration().getSamlSigningCredential()));
    }

    private void addSamlEncryptionDescriptor(SSODescriptor spSso) throws Exception {
        spSso.getKeyDescriptors().add(buildKeyDescriptor(UsageType.ENCRYPTION, configuration.getCredentialConfiguration().getSamlEncryptionCredential()));
    }

    private KeyDescriptor buildKeyDescriptor(UsageType usageType, Credential credential) throws SecurityException {
        KeyDescriptor keyDescriptor = (KeyDescriptor) XMLObjectSupport.buildXMLObject(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        keyDescriptor.setUse(usageType);
        keyDescriptor.setKeyInfo(buildKeyInfo(credential));
        return keyDescriptor;
    }

    private KeyInfo buildKeyInfo(Credential credential) throws SecurityException {
        return keyInfoGeneratorFactory.newInstance().generate(credential);
    }

}
