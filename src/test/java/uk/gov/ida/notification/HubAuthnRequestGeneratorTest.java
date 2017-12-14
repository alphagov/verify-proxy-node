package uk.gov.ida.notification;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.pki.SigningCredential;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class HubAuthnRequestGeneratorTest extends SamlInitializedTest {
    @Test
    public void shouldGenerateHubAuthnRequestGivenEidas () throws Throwable {
        EidasAuthnRequestTranslator translator = new EidasAuthnRequestTranslator("http://proxy-node.uk", "http://hub.uk");
        TestKeyPair keyPair = new TestKeyPair();
        SamlObjectSigner samlObjectSigner = new SamlObjectSigner(new SigningCredential(keyPair.getPublicKey(), keyPair.getPrivateKey()));
        HubAuthnRequestGenerator hubAuthnRequestGenerator = new HubAuthnRequestGenerator(translator, samlObjectSigner);
        EidasAuthnRequest eidasRequest = new EidasAuthnRequest(
                "request-id",
                "http://connector.eu",
                "http://proxy-node.uk",
                SPTypeEnumeration.PUBLIC,
                EidasConstants.EIDAS_LOA_SUBSTANTIAL,
                Collections.emptyList()
        );

        AuthnRequest hubAuthnRequest = hubAuthnRequestGenerator.generate(eidasRequest);

        assertEquals("http://proxy-node.uk", hubAuthnRequest.getIssuer().getValue());
    }
}
