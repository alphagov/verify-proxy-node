package uk.gov.ida.notification.stubconnector;

import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.transforms.Transforms;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.GetPublicKeyRequest;
import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.deserializers.OpenSamlXMLObjectUnmarshaller;
import uk.gov.ida.saml.deserializers.parser.SamlObjectParser;
import uk.gov.ida.saml.security.CredentialFactorySignatureValidator;
import uk.gov.ida.saml.security.SigningCredentialFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;

import static org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1;
import static org.opensaml.xmlsec.encryption.support.EncryptionConstants.ALGO_ID_DIGEST_SHA256;
import static org.opensaml.xmlsec.signature.support.SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
import static org.opensaml.xmlsec.signature.support.SignatureConstants.XMLSIG_NS;

public class KmsTest {

    private final String KMS_KEY_ID = "dcdc859f-78f5-4752-a6aa-feba894d5d37";
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testSigningWithKMS() {
        try {
            // Currently getting these by running aws-vault. DO NOT COMMIT THE VALUES IF YOU ADD THEM!
            environmentVariables.set("AWS_SECURITY_TOKEN", "");
            environmentVariables.set("AWS_SESSION_TOKEN", "");
            environmentVariables.set("AWS_REGION", "eu-west-1");
            environmentVariables.set("AWS_DEFAULT_REGION", "eu-west-1");
            environmentVariables.set("AWS_ACCESS_KEY_ID", "");
            environmentVariables.set("AWS_SECRET_ACCESS_KEY","");

            Security.addProvider(new BouncyCastleProvider());
            IdaSamlBootstrap.bootstrap();

            AuthnRequest authnRequest = AuthnRequestBuilder.anAuthnRequest().withoutSignatureElement().build();

            // Create opensaml AuthnRequest without signature, from file. This is to simulate if we'd created one in an
            // app somewhere and needed to sign it.
            File unsignedFile = new File(getClass().getClassLoader().getResource("unsigned_authn.xml").getFile());
            byte[] unsignedBytes = Files.readAllBytes(unsignedFile.toPath());
            OpenSamlXMLObjectUnmarshaller<AuthnRequest> unmarshaller = new OpenSamlXMLObjectUnmarshaller<>(new SamlObjectParser());
            String unsignedString = new String(unsignedBytes);
            AuthnRequest authnRequestFromFile = unmarshaller.fromString(unsignedString);

            // Get the XML string of the Authn request, ready to pass to a canonicalizer
            Element authnRequestElement = XMLObjectSupport.marshall(authnRequest);
            String serializedAuthnRequest = SerializeSupport.nodeToString(authnRequestElement);

            // Create an exclusive canonicalizer
            Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            // Canonicalize the Authn request into a byte array.
            byte[] canonXmlBytes =  canon.canonicalize(serializedAuthnRequest.getBytes());

            // Get the SHA-256 digest of the canonicialized Authn request and base64 encoded it. This value will be
            // inserted into the SignedInfo element of the Signature.
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(canonXmlBytes);
            String b64Digest = new String(Base64.getEncoder().encode(digest));

            // Get the XML doc of the marshalled Authn request. This is so we can add the signature we're going to
            // generate to it.
            Document doc = authnRequestElement.getOwnerDocument();

            // Create the signature element and append it to the Authn request. This will be the root of our sub-doc.
            Element signature = doc.createElementNS(XMLSIG_NS,"ds:Signature");
            authnRequestElement.appendChild(signature);

            // It's important the SignedInfo block is created with the xmlsig_ns namespace. Without this (just using
            // `doc.createElement()`) the namespace declaration isn't included when we canonicalize the SignedInfo.
            Element signedInfo = doc.createElementNS(XMLSIG_NS,"ds:SignedInfo");
            signature.appendChild(signedInfo);

            Element canonicalizationMethod = doc.createElement("ds:CanonicalizationMethod");
            canonicalizationMethod.setAttribute("Algorithm", ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            signedInfo.appendChild(canonicalizationMethod);

            // This is the URI to use for RSASSA-PSS. It's Confusing. The opensaml library doesn't seem to specify a URI
            // for it, however Apache does, so pulling from there.
            Element signatureMethod = doc.createElement("ds:SignatureMethod");
            signatureMethod.setAttribute("Algorithm", ALGO_ID_SIGNATURE_RSA_SHA256_MGF1);
            signedInfo.appendChild(signatureMethod);

            // This is just pulling the ID from the Authn request. It already has one as the XML the Authn request is
            // loaded from was originally already signed. If it didn't already exist, we could just create a UUID and
            // add it to the Authn request element and reference it here.
            Element reference = doc.createElement("ds:Reference");
            reference.setAttribute("URI", "#" + authnRequest.getID());
            signedInfo.appendChild(reference);

            Element transforms = doc.createElement("ds:Transforms");
            reference.appendChild(transforms);

            Element transform1 = doc.createElement("ds:Transform");
            transform1.setAttribute("Algorithm", Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            transforms.appendChild(transform1);

            Element transform2 = doc.createElement("ds:Transform");
            transform2.setAttribute("Algorithm", Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
            transforms.appendChild(transform2);

            Element digestMethod = doc.createElement("ds:DigestMethod");
            digestMethod.setAttribute("Algorithm", ALGO_ID_DIGEST_SHA256);
            reference.appendChild(digestMethod);

            // This adds the digest of the canonicalized Authn request we calculated earlier.
            Element digestValue = doc.createElement("ds:DigestValue");
            digestValue.appendChild(doc.createTextNode(b64Digest));
            reference.appendChild(digestValue);

            // Now get the canonicalized version of the signed info block. This is what we'll end up signing.
            byte[] canonSignedInfoBytes = canon.canonicalizeSubtree(signedInfo);

            MessageDigest signedInfoMd = MessageDigest.getInstance("SHA-256");
            byte[] signedInfoDigest = signedInfoMd.digest(canonSignedInfoBytes);
            String signedInfoB64Digest = new String(Base64.getEncoder().encode(signedInfoDigest));

            // Sign the canonicalized bytes of the SignedInfo block. Originally I thought I needed to sign the base64
            // encoded digest of the c14nzed SignedInfo block, however the validation fell over. It works with this.
            // Shrug.
            // This sets up and uses the KmsClient, with a hard coded KeyId that I already created.
            // Message Type can be "RAW" or "DIGEST". We need to use "RAW".
            // The signing algorithm string is specified in the AWS docs.
            KmsClient client = KmsClient.create();
            SignRequest signRequest = SignRequest.builder()
                    .messageType("DIGEST")
                    .keyId(KMS_KEY_ID)
                    .message(
                            SdkBytes.fromInputStream(new ByteArrayInputStream(signedInfoDigest)) // Is this right? Should we be signing the B64 of the digest of this?
                    )
                    .signingAlgorithm("RSASSA_PSS_SHA_256")
                    .build();
            SignResponse signResponse = client.sign(signRequest);
            String signedInfoSignatureB64 = new String(Base64.getEncoder().encode(signResponse.signature().asByteArray()));

            // Add SignatureValue element. The signature value comes from above. It should be included in the XML as
            // base64.
            Element signatureValue = doc.createElement("ds:SignatureValue");
            signatureValue.appendChild(doc.createTextNode(signedInfoSignatureB64));
            signature.appendChild(signatureValue);

            // Add keyInfo element
            Element keyInfo = doc.createElement("ds:KeyInfo");
            signature.appendChild(keyInfo);

            // This will add the public key directly, rather than using an X509 certificate. It was proving to be a pain
            // to get a cert so I just hard coded the key in. This could be updated in future.
            Element keyValue = doc.createElement("ds:KeyValue");
            keyInfo.appendChild(keyValue);

            Element rsaKeyValue = doc.createElement("ds:RSAKeyValue");
            keyValue.appendChild(rsaKeyValue);

            // Get the public key from KMS using the API. Note the same keyId as when signing, above.
            GetPublicKeyRequest getPublicKeyRequest = GetPublicKeyRequest.builder()
                    .keyId(KMS_KEY_ID)
                    .build();
            GetPublicKeyResponse getPublicKeyResponse = client.getPublicKey(getPublicKeyRequest);
            client.close();
            SdkBytes sdkBytes = getPublicKeyResponse.publicKey();
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(sdkBytes.asByteArray()));

            // Get the modulus from the public key. When getting a byteArray from a big int, the first byte is the sign
            // (+ or -) of the value. We don't want it so we remove it. This means that the modulus is no 384 bytes
            // instead of 385, which is what we want (key length / 8, or 3072/8)
            byte[] signedModulusBigIntBytes = publicKey.getModulus().toByteArray();
            byte[] unsignedBigIntBytes = new byte[384];
            System.arraycopy(signedModulusBigIntBytes, 1, unsignedBigIntBytes, 0, 384);
            String modulusB64 = new String(Base64.getEncoder().encode(signedModulusBigIntBytes));

            // Get the exponent from the public key. Not sure why we don't need to remove the first byte, like the
            // modulus, but we don't. Maybe because it's smaller.
            byte[] signedExponentBigIntBytes = publicKey.getPublicExponent().toByteArray();
            String exponentB64 = new String(Base64.getEncoder().encode(signedExponentBigIntBytes));

            // Create elements for the modulus and exponent and add them to the keyValue in the XML.
            Element modulus = doc.createElement("ds:Modulus");
            modulus.appendChild(doc.createTextNode(modulusB64));
            rsaKeyValue.appendChild(modulus);

            Element exponent = doc.createElement("ds:Exponent");
            exponent.appendChild(doc.createTextNode(exponentB64));
            rsaKeyValue.appendChild(exponent);

            // Unmarshall the XML back into an opensaml Authn request. This is so we can make sure we've created a valid
            // object. It also means that we can be more sure future operations will be happening to a "familiar"
            // object.
            String serializedSignedAuthnRequest = SerializeSupport.nodeToString(authnRequestElement);
            AuthnRequest signedAuthnRequest = unmarshaller.fromString(serializedSignedAuthnRequest);

            // Verify the signature outside of the context of opensaml as a sanity check.
            java.security.Signature s = java.security.Signature.getInstance("SHA256withRSA/PSS", "BC");
            s.initVerify(publicKey);
            s.update(canonSignedInfoBytes);
            if (!s.verify(signResponse.signature().asByteArray())) {
                throw new RuntimeException("The thing is bad.");
            }

            // Create a signatureValidator using our publicKey. This was pinched from some of our tests.
            CredentialFactorySignatureValidator signatureValidator = new CredentialFactorySignatureValidator(new SigningCredentialFactory(
                    entityId -> Collections.singletonList(publicKey)));

            // Use it to validate the opensaml object.
            if (!signatureValidator.validate(signedAuthnRequest, null, AuthnRequest.DEFAULT_ELEMENT_NAME)) {
                throw new RuntimeException("The opensaml signature is bad.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
