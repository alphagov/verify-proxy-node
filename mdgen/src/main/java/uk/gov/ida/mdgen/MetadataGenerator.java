package uk.gov.ida.mdgen;

import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
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
import picocli.CommandLine;
import se.swedenconnect.opensaml.pkcs11.PKCS11Provider;
import se.swedenconnect.opensaml.pkcs11.PKCS11ProviderFactory;
import se.swedenconnect.opensaml.pkcs11.configuration.PKCS11ProviderConfiguration;
import se.swedenconnect.opensaml.pkcs11.credential.PKCS11Credential;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.concurrent.Callable;

public class MetadataGenerator implements Callable<Void> {
    private final Logger LOG = LoggerFactory.getLogger(MetadataGenerator.class);
    private BasicX509Credential signingCredential;
    private X509KeyInfoGeneratorFactory keyInfoGeneratorFactory;

    private enum NodeType {
        connector(SPSSODescriptor.DEFAULT_ELEMENT_NAME, AssertionConsumerService.DEFAULT_ELEMENT_NAME),
        proxy(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, SingleSignOnService.DEFAULT_ELEMENT_NAME);

        private final QName ssoQname;
        private final QName endpointQname;

        NodeType(QName ssoQname, QName endpointQname) {
            this.ssoQname = ssoQname;
            this.endpointQname = endpointQname;
        }
    }

    private enum SigningAlgoType {
        rsa(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256),
        rsapss(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1),
        ecdsa(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA256);

        private final String uri;

        SigningAlgoType(String uri) {
            this.uri = uri;
        }
    }

    private enum CredentialType {
        file,
        hsm;
    }

    @CommandLine.Parameters(index = "0", description = "Type of node")
    private NodeType nodeType;

    @CommandLine.Parameters(index = "1", description = "Public X509 cert corresponding to private key")
    private File signingCertFile;

    @CommandLine.Parameters(index = "2", description = "Entity ID of node")
    private URL entityID;

    @CommandLine.Option(names = "--output", description = "Output file")
    private File outputFile;

    @CommandLine.Option(names = "--endpoint", description = "SingleSignOn/AssertionConsumer endpoint URL")
    private URL endpointURI;

    @CommandLine.Option(names = "--algorithm", description = "Signing algorithm")
    private SigningAlgoType signingAlgo = SigningAlgoType.rsa;

    @CommandLine.Option(names = "--credential", description = "Type of private key credential")
    private CredentialType credentialType = CredentialType.file;

    @CommandLine.Option(names = "--key-file", description = "Private key file")
    private File keyFile;

    @CommandLine.Option(names = "--key-pass", description = "Passphrase for encrypted private key")
    private String keyPass = "";

    @CommandLine.Option(names = "--hsm-token-label", description = "HSM name")
    private String hsmTokenLabel = "softhsm";

    @CommandLine.Option(names = "--hsm-key-label", description = "HSM key label")
    private String hsmKeyLabel = "private_key";

    @CommandLine.Option(names = "--hsm-slot-index", description = "HSM slot index")
    private Integer hsmSlotIndex = 0;

    @CommandLine.Option(names = "--hsm-pin", description = "HSM token PIN")
    private String hsmPin = "1234";

    @CommandLine.Option(names = "--hsm-module", description = "HSM shared object module")
    private String hsmModule = "/usr/lib/hsm/libsofthsm2.so";

    @Override
    public Void call() throws Exception {
        if (endpointURI == null) {
            endpointURI = new URL(entityID.getProtocol(), entityID.getHost(), "/SAML2/POST");
        }

        X509Certificate signingCert = X509Support.decodeCertificate(signingCertFile);

        if (signingAlgo == SigningAlgoType.rsapss) {
            Security.addProvider(new BouncyCastleProvider());
        }

        switch (credentialType) {
            case file:
                signingCredential = getSigningCredentialFromFile(signingCert, keyFile, keyPass);
                break;
            case hsm:
                signingCredential = getSigningCredentialFromPKCS11(signingCert);
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

    public static void main(String[] args) throws InitializationException {
        InitializationService.initialize();
        CommandLine.call(new MetadataGenerator(), args);
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

    private BasicX509Credential getSigningCredentialFromPKCS11(X509Certificate cert) throws Exception {
        LOG.info("Using credential from PKCS11: module={} token={} key={} slot={} pin={}", hsmModule, hsmTokenLabel, hsmKeyLabel, hsmSlotIndex, hsmPin);
        PKCS11ProviderConfiguration config = new PKCS11ProviderConfiguration();
        config.setLibrary(hsmModule);
        config.setName(hsmTokenLabel);
        config.setSlotListIndex(hsmSlotIndex);
        PKCS11ProviderFactory providerFactory = new PKCS11ProviderFactory(
            config,
            configData -> Security.getProvider("SunPKCS11").configure("--"+configData)
        );
        PKCS11Provider provider = providerFactory.createInstance();
        for (String name : provider.getProviderNameList()) {
            LOG.info("Provider: " + name);
        }
        return new PKCS11Credential(cert, provider.getProviderNameList(), hsmKeyLabel, hsmPin);
    }

    private EntityDescriptor buildEntityDescriptor() throws SecurityException, SignatureException, MarshallingException {
        SSODescriptor ssoDescriptor = getSsoDescriptor();

        EntityDescriptor entityDescriptor = (EntityDescriptor) XMLObjectSupport.buildXMLObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        entityDescriptor.setEntityID(entityID.toString());
        entityDescriptor.setValidUntil(DateTime.now().plusDays(365));
        entityDescriptor.getRoleDescriptors().add(ssoDescriptor);

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
        signingParams.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_OMIT_COMMENTS);
        signingParams.setSigningCredential(signingCredential);
        signingParams.setKeyInfoGenerator(keyInfoGeneratorFactory.newInstance());

        SignatureSupport.signObject(entityDescriptor, signingParams);

        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(entityDescriptor.getSignature());
        SignatureValidator.validate(entityDescriptor.getSignature(), signingCredential);
    }

    private SSODescriptor getSsoDescriptor() throws SecurityException {
        LOG.info("Generating metadata for {} node", nodeType);

        XMLObject endpointObject = XMLObjectSupport.buildXMLObject(nodeType.endpointQname);
        Endpoint endpoint = (Endpoint) endpointObject;
        endpoint.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        endpoint.setLocation(endpointURI.toString());

        XMLObject ssoObject = XMLObjectSupport.buildXMLObject(nodeType.ssoQname);
        SSODescriptor ssoDescriptor = (SSODescriptor) ssoObject;
        ssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20_NS);

        switch (nodeType) {
            case connector:
                AssertionConsumerService assertionConsumerService = (AssertionConsumerService) endpoint;
                assertionConsumerService.setIndex(1);
                assertionConsumerService.setIsDefault(true);

                SPSSODescriptor spSso = (SPSSODescriptor) ssoDescriptor;
                spSso.getAssertionConsumerServices().add(assertionConsumerService);
                spSso.getKeyDescriptors().add(buildKeyDescriptor(UsageType.SIGNING, signingCredential));
                spSso.getKeyDescriptors().add(buildKeyDescriptor(UsageType.ENCRYPTION, signingCredential));
                break;

            case proxy:
                SingleSignOnService singleSignOnService = (SingleSignOnService) endpoint;
                IDPSSODescriptor idpSso = (IDPSSODescriptor) ssoDescriptor;
                idpSso.getSingleSignOnServices().add(singleSignOnService);
                idpSso.getKeyDescriptors().add(buildKeyDescriptor(UsageType.SIGNING, signingCredential));
                break;
        }

        return ssoDescriptor;
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
