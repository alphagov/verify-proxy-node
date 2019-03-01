package uk.gov.ida.mdgen;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Support;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;
import se.litsec.opensaml.utils.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class MetadataGenerator implements Callable<Void> {
    private final Logger LOG = LoggerFactory.getLogger(MetadataGenerator.class);
    private final Yaml yaml = new Yaml();
    private BasicX509Credential signingCredential;
    private X509KeyInfoGeneratorFactory keyInfoGeneratorFactory;

    enum NodeType { connector, proxy }
    enum CredentialType { file, cloudhsm }

    enum SigningAlgoType {
        rsa(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256),
        rsapss(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1),
        ecdsa(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA256);

        private final String uri;

        SigningAlgoType(String uri) {
            this.uri = uri;
        }
    }

    @CommandLine.Parameters(index = "0", description = "Type of node")
    private NodeType nodeType;

    @CommandLine.Parameters(index = "1", description = "YAML definition file")
    private File yamlFile;

    @CommandLine.Parameters(index = "2", description = "Public X509 cert corresponding to private key")
    private File signingCertFile;

    @CommandLine.Option(names = "--output", description = "Output file")
    private File outputFile;

    @CommandLine.Option(names = "--algorithm", description = "Signing algorithm")
    private SigningAlgoType signingAlgo = SigningAlgoType.rsa;

    @CommandLine.Option(names = "--credential", description = "Type of private key credential")
    private CredentialType credentialType = CredentialType.file;

    @CommandLine.Option(names = "--key-file", description = "Private key file")
    private File keyFile;

    @CommandLine.Option(names = "--key-pass", description = "Passphrase for encrypted private key")
    private String keyPass = "";

    @CommandLine.Option(names = "--hsm-key-label", description = "HSM key label")
    private String hsmKeyLabel = "private_key";

    public static void main(String[] args) throws InitializationException {
        InitializationService.initialize();
        CommandLine.call(new MetadataGenerator(), args);
    }

    @Override
    public Void call() throws Exception {
        X509Certificate signingCert = X509Support.decodeCertificate(signingCertFile);

        if (signingAlgo == SigningAlgoType.rsapss) {
            Security.addProvider(new BouncyCastleProvider());
        }

        switch (credentialType) {
            case file:
                signingCredential = getSigningCredentialFromFile(signingCert, keyFile, keyPass);
                break;
            case cloudhsm:
                signingCredential = getSigningCredentialFromCloudHSM(signingCert);
                break;
        }

        if (signingCredential.getPublicKey() instanceof ECPublicKey) {
            LOG.warn("Credential public key is of EC type, using ECDSA signing algorithm");
            signingAlgo = SigningAlgoType.ecdsa;
        }

        OutputStream outputStream;

        if (outputFile == null) {
            outputStream = System.out;
        } else {
            outputStream = new FileOutputStream(outputFile);
        }

        keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);

        XMLObjectSupport.marshallToOutputStream(buildEntityDescriptor(), outputStream);
        return null;
    }

    private BasicX509Credential getSigningCredentialFromCloudHSM(X509Certificate cert) throws Exception {
        Provider caviumProvider = (Provider) ClassLoader.getSystemClassLoader()
            .loadClass("com.cavium.provider.CaviumProvider")
            .getConstructor()
            .newInstance();
        Security.addProvider(caviumProvider);
        JCEMapper.setProviderId("Cavium");
        KeyStore cloudHsmStore = KeyStore.getInstance("Cavium");
        cloudHsmStore.load(null, null);
        return new BasicX509Credential(cert, (PrivateKey) cloudHsmStore.getKey(hsmKeyLabel, null));
    }

    private BasicX509Credential getSigningCredentialFromFile(X509Certificate cert, File keyFile, String keyPass) {
        if (keyFile == null) {
            LOG.error("Need to specify keyFile when credential type is file");
            System.exit(1);
        }
        LOG.info("Using credential from file: keyFile={} keyPass={}", keyFile, keyPass);
        try {
            PrivateKey key = KeySupport.decodePrivateKey(keyFile, keyPass.toCharArray());
            return new BasicX509Credential(cert, key);
        } catch(Exception e) {
            LOG.error("Could not read from private key file, is there a passphrase?\nException: {}", e.getMessage());
            System.exit(1);
        }
        return null;
    }

    private String renderTemplate(String template, Map values) {
        Mustache mustache = new DefaultMustacheFactory().compile(template);
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, values);
        stringWriter.flush();
        return stringWriter.toString();
    }

    private EntityDescriptor buildEntityDescriptor() throws Exception {
        Map yamlMap = yaml.load(new FileInputStream(yamlFile));
        String xml = renderTemplate(nodeType.toString() + "_template.xml.mustache", yamlMap);
        EntityDescriptor entityDescriptor = ObjectUtils.unmarshall(new ByteArrayInputStream(xml.getBytes()), EntityDescriptor.class);
        entityDescriptor.setID(UUID.randomUUID().toString());
        entityDescriptor.setValidUntil(DateTime.now().plusDays(365));
        updateSsoDescriptor(entityDescriptor);
        sign(entityDescriptor);
        return entityDescriptor;
    }

    private void sign(EntityDescriptor entityDescriptor) throws SecurityException, MarshallingException, SignatureException {
        LOG.info("Attempting to sign metadata");
        LOG.info("\n  Algorithm: {}\n  Credential: {}\n",
            signingAlgo.uri,
            signingCredential.getEntityCertificate().getSubjectDN().getName());

        SignatureSigningParameters signingParams = new SignatureSigningParameters();
        signingParams.setSignatureAlgorithm(signingAlgo.uri);
        signingParams.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signingParams.setSigningCredential(signingCredential);
        signingParams.setKeyInfoGenerator(keyInfoGeneratorFactory.newInstance());

        SignatureSupport.signObject(entityDescriptor, signingParams);

        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(entityDescriptor.getSignature());
        SignatureValidator.validate(entityDescriptor.getSignature(), signingCredential);
    }

    private void updateSsoDescriptor(EntityDescriptor entityDescriptor) throws SecurityException {
        switch (nodeType) {
            case connector:
                SPSSODescriptor spSso = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
                spSso.getKeyDescriptors().add(buildKeyDescriptor(UsageType.SIGNING, signingCredential));
                spSso.getKeyDescriptors().add(buildKeyDescriptor(UsageType.ENCRYPTION, signingCredential));
                break;

            case proxy:
                IDPSSODescriptor idpSso = entityDescriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
                idpSso.getKeyDescriptors().add(buildKeyDescriptor(UsageType.SIGNING, signingCredential));
                break;
        }
    }

    private KeyDescriptor buildKeyDescriptor(UsageType usageType, BasicX509Credential credential) throws SecurityException {
        KeyDescriptor keyDescriptor = (KeyDescriptor) XMLObjectSupport.buildXMLObject(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        keyDescriptor.setUse(usageType);
        keyDescriptor.setKeyInfo(buildKeyInfo(credential));
        return keyDescriptor;
    }

    private KeyInfo buildKeyInfo(Credential credential) throws SecurityException {
        KeyInfo keyInfo = keyInfoGeneratorFactory.newInstance().generate(credential);
        return keyInfo;
    }
}
