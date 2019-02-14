package uk.gov.ida.notification.eidassaml;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.eidassaml.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;

public class EidasSamlResourceTest {

    private final static String TEST_CONNECTOR_DESTINATION = "https://stub_country.acme.eu/stub-country-one/destination";

    private static EidasAuthnRequestValidator eidasAuthnRequestValidator = Mockito.mock(EidasAuthnRequestValidator.class);

    private static SamlRequestSignatureValidator samlRequestSignatureValidator = Mockito.mock(SamlRequestSignatureValidator.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new EidasSamlResource(eidasAuthnRequestValidator, samlRequestSignatureValidator, TEST_RP_PUBLIC_ENCRYPTION_CERT, TEST_CONNECTOR_DESTINATION))
            .build();

    @Before
    public void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldReturnRequestIdAndIssuer() throws Exception {
        AuthnRequest authnRequest = ObjectUtils.createSamlObject(AuthnRequest.class);
        Issuer issuer = ObjectUtils.createSamlObject(Issuer.class);
        issuer.setValue("issuer");
        authnRequest.setID("request_id");
        authnRequest.setIssuer(issuer);

        EidasSamlParserRequest request = new EidasSamlParserRequest(Base64.encodeAsString(ObjectUtils.toString(authnRequest)));
        EidasSamlParserResponse response = resources.target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(EidasSamlParserResponse.class);

        assertEquals(response.getRequestId(), "request_id");
        assertEquals(response.getIssuer(), "issuer");
        assertEquals(response.getConnectorEncryptionPublicCertificate(), TEST_RP_PUBLIC_ENCRYPTION_CERT);
        assertEquals(response.getDestination(), TEST_CONNECTOR_DESTINATION);
    }
}
