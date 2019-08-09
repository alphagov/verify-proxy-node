package uk.gov.ida.notification.session;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import net.minidev.json.JSONObject;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

public class JWKSetGenerator {
    public static void main(String... args) throws JOSEException {
        String jwkSet = JWKSetGenerator.createJWKSet();
        System.out.println("jwk set:");
        System.out.println(jwkSet);
        System.out.println("base64 encoded jwk set:");
        System.out.println(Base64.getEncoder().encodeToString(jwkSet.getBytes()));
    }

    public static String createJWKSet() throws JOSEException {
        Instant now = Instant.now();
        RSAKey sig = makeKey(KeyUse.SIGNATURE, now);
        RSAKey enc = makeKey(KeyUse.ENCRYPTION, now);
        JWKSet jwkSet = new JWKSet(List.of(sig, enc));
        JSONObject jsonObject = jwkSet.toJSONObject(false);
        return jsonObject.toJSONString();
    }

    private static RSAKey makeKey(KeyUse usage, Instant now) throws JOSEException {
        String id = String.format("%s-%d", usage, now.toEpochMilli());
        return new RSAKeyGenerator(2048)
                .keyUse(usage)
                .keyID(id)
                .generate();
    }
}
