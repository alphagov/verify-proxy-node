package uk.gov.ida.notification.translator.saml;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseTestAssertions;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import java.net.URI;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;

public class HubResponseTranslatorTest {

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void translateShouldReturnValidResponseWhenIdentityVerified() {

        HubResponseTranslator translator =
                new HubResponseTranslator(EidasResponseBuilder::instance, "Issuer", "connectorMetadataURL");

        Response response =
            translator.translate(buildHubResponseContainer());

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(response);
        TranslatedHubResponseTestAssertions.assertAttributes(response);
        TranslatedHubResponseTestAssertions.assertResponseForIdentityVerifiedStatus(response);
    }

    @Test
    public void translateShouldReturnResponseForCancelledStatus() {

        HubResponseTranslator translator =
                new HubResponseTranslator(EidasResponseBuilder::instance, "Issuer", "connectorMetadataURL");

        Response response =
            translator.translate(buildTranslatedHubResponseForCancelledStatus());

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(response);
        TranslatedHubResponseTestAssertions.assertResponseForCancelledStatus(response);
    }

    @Test
    public void translateShouldReturnResponseForAuthenticationFailedStatus() {

        HubResponseTranslator translator =
                new HubResponseTranslator(EidasResponseBuilder::instance, "Issuer", "connectorMetadataURL");

        Response response =
                translator.translate(buildTranslatedHubResponseForAuthenticationFailedStatus());

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(response);
        TranslatedHubResponseTestAssertions.assertResponseForAuthenticationFailedStatus(response);
    }

    @Test
    public void translateShouldThrowHubResponseTranslationExceptionWhenAttributesNull() {

        expectedException.expect(HubResponseTranslationException.class);
        expectedException.expectMessage("HubResponseContainer Attributes null.");

        HubResponseTranslator translator =
                new HubResponseTranslator(EidasResponseBuilder::instance, "Issuer", "connectorMetadataURL");

        translator.translate(buildHubResponseContainerWithNoAttributes());
    }

    @Test
    public void translateShouldThrowHubResponseTranslationExceptionWhenRequiredAttributeNull() {

        expectedException.expect(HubResponseTranslationException.class);
        expectedException.expectMessage("HubResponseContainer Attribute Surnames null.");

        HubResponseTranslator translator =
                new HubResponseTranslator(EidasResponseBuilder::instance, "Issuer", "connectorMetadataURL");

        translator.translate(buildHubResponseContainerWithOnlyOneAttribute());
    }

    @Test
    public void translateShouldThrowExceptionWhenRequestError() {

        expectedException.expect(HubResponseTranslationException.class);
        expectedException.expectMessage("Received error status from VSP: ");

        HubResponseTranslator translator =
                new HubResponseTranslator(EidasResponseBuilder::instance, "Issuer", "connectorMetadataURL");

        translator.translate(buildTranslatedHubResponseRequestError());
    }

    @Test
    public void translateShouldThrowExceptionWhenIdentityVerifiedWithLOA1() {

        expectedException.expect(HubResponseTranslationException.class);
        expectedException.expectMessage("Received unsupported LOA from VSP: ");

        HubResponseTranslator translator =
                new HubResponseTranslator(EidasResponseBuilder::instance, "Issuer", "connectorMetadataURL");

        translator.translate(buildHubResponseContainerLOA1());
    }

    private HubResponseContainer buildHubResponseContainerWithNoAttributes() {
        return new HubResponseContainer(
                new HubResponseTranslatorRequest(),
                TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerifiedNoAttributes()
        );
    }

    private HubResponseContainer buildHubResponseContainerWithOnlyOneAttribute() {
        return new HubResponseContainer(
                new HubResponseTranslatorRequest(),
                TranslatedHubResponseBuilder.buildTranslatedHubResponseIncompleteAttributes()
        );
    }

    private HubResponseContainer buildHubResponseContainer() {
        return new HubResponseContainer(buildHubResponseTranslatorRequest(), TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerified());
    }

    private HubResponseContainer buildHubResponseContainerLOA1() {
        return new HubResponseContainer(buildHubResponseTranslatorRequest(), TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerifiedLOA1());
    }

    private HubResponseContainer buildTranslatedHubResponseForCancelledStatus() {
        return new HubResponseContainer(buildHubResponseTranslatorRequest(), TranslatedHubResponseBuilder.buildTranslatedHubResponseCancellation());
    }

    private HubResponseContainer buildTranslatedHubResponseForAuthenticationFailedStatus() {
        return new HubResponseContainer(buildHubResponseTranslatorRequest(), TranslatedHubResponseBuilder.buildTranslatedHubResponseAuthenticationFailed());
    }

    private HubResponseContainer buildTranslatedHubResponseRequestError() {
        return new HubResponseContainer(buildHubResponseTranslatorRequest(), TranslatedHubResponseBuilder.buildTranslatedHubResponseRequestError());
    }

    private HubResponseTranslatorRequest buildHubResponseTranslatorRequest() {
        return new HubResponseTranslatorRequest(
                "",
                "_1234",
                ResponseBuilder.DEFAULT_REQUEST_ID,
                "LEVEL_2",
                URI.create("http://localhost:8081/bob"),
                STUB_COUNTRY_PUBLIC_PRIMARY_CERT);
    }
}
