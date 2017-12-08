package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.HubResponse;


public class HubResponseGenerator {

    private SamlParser parser;
    private ProxyNodeSignatureValidator signatureValidator;
    private CredentialRepository credentialRepository;

    public HubResponseGenerator(SamlParser parser, ProxyNodeSignatureValidator signatureValidator, CredentialRepository credentialRepository) {
        this.parser = parser;
        this.signatureValidator = signatureValidator;
        this.credentialRepository = credentialRepository;
    }

    public HubResponse generate(String responseAsString) throws Throwable {
        String decodeStringResponse = Base64.decodeAsString(responseAsString);
        Response response = parser.parseSamlString(decodeStringResponse);
        Credential hubCredential = credentialRepository.getHubCredential();
        signatureValidator.validate(response, hubCredential);
        return new HubResponse(response);
    }
}
