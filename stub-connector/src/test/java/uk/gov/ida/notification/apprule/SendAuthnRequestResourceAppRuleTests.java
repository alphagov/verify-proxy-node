package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.apprule.base.StubConnectorAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.StubConnectorAppRule;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.saml.SamlParser;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class SendAuthnRequestResourceAppRuleTests extends StubConnectorAppRuleTestBase {

    private static final DropwizardClientRule metadataClientRule = createTestMetadataClientRule();
    private static final StubConnectorAppRule stubConnectorAppRule = createStubConnectorAppRule(metadataClientRule);

    @ClassRule
    public static final RuleChain orderedRules = RuleChain.outerRule(metadataClientRule).around(stubConnectorAppRule);

    @Test
    public void canGenerateAValidEidasAuthnRequest() throws IOException, URISyntaxException {
        final AuthnRequest authnRequest = getValidEidasAuthnRequest();
        Assert.assertTrue(authnRequest.isSigned());
    }

    @Test
    public void canGenerateAnInvalidSignatureEidasAuthnRequest() throws URISyntaxException, IOException {
        final AuthnRequest authnRequest = getInvalidSignatureEidasAuthnRequest();
        Assert.assertTrue(authnRequest.isSigned());
    }

    private AuthnRequest getInvalidSignatureEidasAuthnRequest() throws URISyntaxException, IOException {
        String html = getInvalidSignatureEidasRequest(stubConnectorAppRule);
        String decodedSaml = HtmlHelpers.getValueFromForm(html, "SAMLRequest");
        return new SamlParser().parseSamlString(decodedSaml);
    }

    private AuthnRequest getValidEidasAuthnRequest() throws IOException, URISyntaxException {
        String html = getValidSubstantialEidasRequest(stubConnectorAppRule);
        String decodedSaml = HtmlHelpers.getValueFromForm(html, "SAMLRequest");
        return new SamlParser().parseSamlString(decodedSaml);
    }


}
