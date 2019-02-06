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
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.KeyValue;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
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
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class MetadataGenerator implements Callable<Void> {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private BasicX509Credential signingCredential;

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

    @CommandLine.Option(names = "--hsm-alias", description = "HSM credential alias")
    private String hsmAlias = "vfpn-uk";

    @CommandLine.Option(names = "--hsm-slot", description = "HSM credential slot")
    private String hsmSlot;

    @CommandLine.Option(names = "--hsm-pin", description = "HSM credential PIN")
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

        OutputStream outputStream;

        if (outputFile == null) {
            outputStream = System.out;
        } else {
            outputStream = new FileOutputStream(outputFile);
        }

        XMLObjectSupport.marshallToOutputStream(buildEntityDescriptor(), outputStream);
        return null;
    }

    public static void main(String[] args) throws InitializationException {
        InitializationService.initialize();
        CommandLine.call(new MetadataGenerator(), args);
    }

    private BasicX509Credential getSigningCredentialFromFile(X509Certificate cert, File keyFile, String keyPass) {
        if (keyFile == null) {
            log.severe("Need to specify keyFile when credential type is file");
            System.exit(1);
        }
        log.info(String.format("Using credential from file: keyFile=%s keyPass=%s", keyFile, keyPass));
        try {
            PrivateKey key = KeySupport.decodePrivateKey(keyFile, keyPass.toCharArray());
            return new BasicX509Credential(cert, key);
        } catch(Exception e) {
            log.severe("Could not read from private key file, is there a passphrase?\nException: "+e.getMessage());
            System.exit(1);
        }
        return null;
    }

    private BasicX509Credential getSigningCredentialFromPKCS11(X509Certificate cert) throws Exception {
        log.info(String.format("Using credential from PKCS11: module=%s alias=%s slot=%s pin=%s", hsmModule, hsmAlias, hsmSlot, hsmPin));
        PKCS11ProviderConfiguration config = new PKCS11ProviderConfiguration();
        config.setLibrary(hsmModule);
        config.setName(hsmAlias);
        config.setSlot(hsmSlot);
        PKCS11ProviderFactory providerFactory = new PKCS11ProviderFactory(
            config,
            configData -> Security.getProvider("SunPKCS11").configure("--"+configData)
        );
        PKCS11Provider provider = providerFactory.createInstance();
        for (String name : provider.getProviderNameList()) {
            log.info("Provider: " + name);
        }
        return new PKCS11Credential(cert, provider.getProviderNameList(), hsmAlias, hsmPin);
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
        log.info("Attempting to sign metadata");
        log.info(String.format("\n  Algorithm: %s\n  Credential: %s\n",
            signingAlgo.uri,
            signingCredential.getEntityCertificate().getSubjectDN().getName()));

        SignatureSigningParameters signingParams = new SignatureSigningParameters();
        signingParams.setSignatureAlgorithm(signingAlgo.uri);
        signingParams.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_OMIT_COMMENTS);
        signingParams.setSigningCredential(signingCredential);

        SignatureSupport.signObject(entityDescriptor, signingParams);

        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(entityDescriptor.getSignature());
        SignatureValidator.validate(entityDescriptor.getSignature(), signingCredential);
    }

    private SSODescriptor getSsoDescriptor() throws SecurityException {
        log.info(String.format("Generating metadata for %s node", nodeType));

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
        X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(credential);

        KeyValue keyValue = (KeyValue) XMLObjectSupport.buildXMLObject(KeyValue.DEFAULT_ELEMENT_NAME);
        RSAPublicKey publicKey = (RSAPublicKey) credential.getPublicKey();
        keyValue.setRSAKeyValue(KeyInfoSupport.buildRSAKeyValue(publicKey));
        keyInfo.getKeyValues().add(keyValue);

        return keyInfo;
    }
}
