package uk.gov.ida.notification.session;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.ida.notification.exceptions.JSONWebTokenException;

import javax.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Provider
public class JSONWebTokenService {

    private final KeyPair encryptionKeyPair;
    private final KeyPair signingKeyPair;

    public JSONWebTokenService(KeyPair encryptionKeyPair, KeyPair signingKeyPair) {
        this.encryptionKeyPair = encryptionKeyPair;
        this.signingKeyPair = signingKeyPair;
    }

    public String create(final JWTClaimsSet claimsSet) {
        try {
            SignedJWT signedJWT = createSignedJWT(Objects.requireNonNull(claimsSet, "claimsSet is null"));
            JWEObject jweObject = createEncryptedJweObject(signedJWT);
            return Base64.getEncoder().encodeToString(jweObject.serialize().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new JSONWebTokenException(String.format("Cannot create JWT: %s", e.getMessage()), e);
        }
    }

    public JWTClaimsSet read(final String jweString) {
        try {
            byte[] decoded = Base64.getDecoder().decode(jweString);
            JWEObject jweObject = JWEObject.parse(new String(decoded));
            JWEHeader header = jweObject.getHeader();
            if (!JWEAlgorithm.RSA_OAEP_256.equals(header.getAlgorithm())) {
                throw new SecurityException("JWE Algorithm RSA_OAEP_256 not specified on header");
            }
            if (!EncryptionMethod.A256GCM.equals(header.getEncryptionMethod())) {
                throw new JSONWebTokenException("JWE Encryption Method A256GCM not specified on header");
            }
            jweObject.decrypt(new RSADecrypter(this.encryptionKeyPair.getPrivate()));
            SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
            if (!signedJWT.verify(new RSASSAVerifier((RSAPublicKey) this.signingKeyPair.getPublic()))) {
                throw new JSONWebTokenException("JWT signing not verified");
            }
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
            Date expirationTime = jwtClaimsSet.getExpirationTime();
            if (expirationTime.before(new Date())) {
                throw new JSONWebTokenException("JWT has expired");
            }
            return jwtClaimsSet;
        } catch (Exception e) {
            throw new JSONWebTokenException("Could not read JWT", e);
        }
    }

    private JWEObject createEncryptedJweObject(SignedJWT signedJWT) throws JOSEException {
        JWEObject jweObject = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                        .contentType("JWT")
                        .build(),
                new Payload(signedJWT));
        jweObject.encrypt(new RSAEncrypter((RSAPublicKey) this.encryptionKeyPair.getPublic()));
        return jweObject;
    }

    private SignedJWT createSignedJWT(JWTClaimsSet claimsSet) throws JOSEException {
        PrivateKey signing = this.signingKeyPair.getPrivate();
        String kid = String.valueOf(UUID.randomUUID().toString());
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(kid).build();
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(new RSASSASigner(signing));
        return signedJWT;
    }
}
