package uk.gov.ida.notification.resources;

import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.ida.notification.exceptions.JSONWebTokenException;
import uk.gov.ida.notification.session.JSONWebTokenService;
import uk.gov.ida.notification.session.JWKSetConfiguration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class JSONWebTokenServiceTest {

    private static KeyPair encryptionKeyPair;
    private static KeyPair signingKeyPair;

    @BeforeClass
    public static void setUp() throws Exception {
        String path = JSONWebTokenServiceTest.class.getClassLoader().getResource("jws-set.json").getPath();
        final String expectedMetadata = new String(Files.readAllBytes(Paths.get(path)));
        JWKSetConfiguration configuration = new JWKSetConfiguration(expectedMetadata);
        encryptionKeyPair = configuration.getEncryptionKeyPair();
        signingKeyPair = configuration.getSigningKeyPair();
    }

    @Test
    public void createAndReadClaimsSet() {
        JSONWebTokenService helper = new JSONWebTokenService(encryptionKeyPair, signingKeyPair);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .expirationTime(Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()))
                        .claim("foo", "bar")
                        .issuer("issuer")
                        .build();
        String token = helper.create(claimsSet);
        JWTClaimsSet result = helper.read(token);
        assertThat(result.getClaim("foo")).isEqualTo("bar");
        assertThat(result.getIssuer()).isEqualTo("issuer");
    }

    @Test(expected = JSONWebTokenException.class)
    public void throwSecurityExceptionWhenReadingInvalidToken() {
        new JSONWebTokenService(encryptionKeyPair, signingKeyPair).read("some bad value");
    }
}