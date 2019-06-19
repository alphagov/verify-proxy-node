package uk.gov.ida.notification.translator.saml;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AttributesBuilder;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseTestAssertions;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.AttributesBuilder.createAttribute;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.AttributesBuilder.createDateTime;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder.buildTranslatedHubResponseAuthenticationFailed;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder.buildTranslatedHubResponseCancellation;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerified;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder.buildTranslatedHubResponseRequestError;
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

    private static final HubResponseTranslator TRANSLATOR =
            new HubResponseTranslator(EidasResponseBuilder::instance, "Issuer", "connectorMetadataURL");

    private AttributesBuilder attributesBuilder;

    @Before
    public void setUp() {
        this.attributesBuilder = new AttributesBuilder();
    }


    @Test
    public void translateShouldReturnValidResponseWhenIdentityVerified() {
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(buildTranslatedHubResponseIdentityVerified());

        final Response identityVerifiedResponse = TRANSLATOR.getTranslatedHubResponse(hubResponseContainer);

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(identityVerifiedResponse);
        TranslatedHubResponseTestAssertions.checkAllAttributesValid(identityVerifiedResponse);
        TranslatedHubResponseTestAssertions.checkResponseStatusCodeValidForIdentityVerifiedStatus(identityVerifiedResponse);
    }

    @Test
    public void translateShouldReturnResponseForCancelledStatus() {
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(buildTranslatedHubResponseCancellation());

        Response cancelledResponse = TRANSLATOR.getTranslatedHubResponse(hubResponseContainer);

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(cancelledResponse);
        TranslatedHubResponseTestAssertions.checkResponseStatusCodeValidForCancelledStatus(cancelledResponse);
    }

    @Test
    public void translateShouldReturnResponseForAuthenticationFailedStatus() {
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(buildTranslatedHubResponseAuthenticationFailed());

        Response authnFailedResponse = TRANSLATOR.getTranslatedHubResponse(hubResponseContainer);

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(authnFailedResponse);
        TranslatedHubResponseTestAssertions.checkResponseStatusCodeValidForAuthenticationFailedStatus(authnFailedResponse);
    }

    @Test
    public void translateShouldThrowWhenNoFirstNamePresent() {
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.withoutFirstName().build());

        assertThatThrownBy(() -> TRANSLATOR.getTranslatedHubResponse(hubResponseContainer))
                .isInstanceOf(HubResponseTranslationException.class)
                .hasMessageContaining("No verified current first name present");
    }

    @Test
    public void translateShouldThrowWhenNoSurnamePresent() {
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.withoutLastName().build());

        assertThatThrownBy(() -> TRANSLATOR.getTranslatedHubResponse(hubResponseContainer))
                .isInstanceOf(HubResponseTranslationException.class)
                .hasMessageContaining("No verified current surname present");
    }

    @Test
    public void translateShouldThrowWhenNoDateOfBirthPresent() {
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.withoutDateOfBirth().build());

        assertThatThrownBy(() -> TRANSLATOR.getTranslatedHubResponse(hubResponseContainer))
                .isInstanceOf(HubResponseTranslationException.class)
                .hasMessageContaining("No verified current date of birth present");
    }

    @Test
    public void translateShouldThrowWhenNoCurrentFirstName() {
        final DateTime validTo = createDateTime(2018, 1, 1, 0, 0);
        final Attribute<String> firstName = createAttribute(AttributesBuilder.FIRST_NAME, true, AttributesBuilder.VALID_FROM, validTo);
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.withFirstName(firstName).build());

        assertThatThrownBy(() -> TRANSLATOR.getTranslatedHubResponse(hubResponseContainer))
                .isInstanceOf(HubResponseTranslationException.class)
                .hasMessageContaining("No verified current first name present");
    }

    @Test
    public void translateShouldThrowWhenCurrentFirstNameNotValid() {
        final Attribute<String> firstName = createAttribute(AttributesBuilder.FIRST_NAME, false, AttributesBuilder.VALID_FROM, null);
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.withFirstName(firstName).build());

        assertThatThrownBy(() -> TRANSLATOR.getTranslatedHubResponse(hubResponseContainer))
                .isInstanceOf(HubResponseTranslationException.class)
                .hasMessageContaining("No verified current first name present");
    }

    @Test
    public void translateShouldIgnoreExpiredExtraNames() {
        final DateTime validTo = AttributesBuilder.VALID_FROM;
        final DateTime validFrom = AttributesBuilder.VALID_FROM.minusYears(3);
        final Attribute<String> firstName = createAttribute("Expired", true, validFrom, validTo);
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.addFirstName(firstName).build());

        final Response response = TRANSLATOR.getTranslatedHubResponse(hubResponseContainer);

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(response);
        TranslatedHubResponseTestAssertions.checkAllAttributesValid(response);
        TranslatedHubResponseTestAssertions.checkResponseStatusCodeValidForIdentityVerifiedStatus(response);
    }

    @Test
    public void translateShouldIgnoreUnverifiedExtraNames() {
        final Attribute<String> firstName = createAttribute("Unverified", false, AttributesBuilder.VALID_FROM, null);
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.addFirstName(firstName).build());

        final Response response = TRANSLATOR.getTranslatedHubResponse(hubResponseContainer);

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(response);
        TranslatedHubResponseTestAssertions.checkAllAttributesValid(response);
        TranslatedHubResponseTestAssertions.checkResponseStatusCodeValidForIdentityVerifiedStatus(response);
    }

    @Test
    public void translateShouldThrowExceptionWhenRequestError() {
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(buildTranslatedHubResponseRequestError());

        assertThatThrownBy(() -> TRANSLATOR.getTranslatedHubResponse(hubResponseContainer))
                .isInstanceOf(HubResponseTranslationException.class)
                .hasMessageContaining("Received error status from VSP: ");
    }

    @Test
    public void translateShouldThrowExceptionWhenIdentityVerifiedWithLOA1() {
        final HubResponseContainer hubResponseContainer =
                buildHubResponseContainer(new TranslatedHubResponseBuilder().withLevelOfAssurance(VspLevelOfAssurance.LEVEL_1).build());

        assertThatThrownBy(() -> TRANSLATOR.getTranslatedHubResponse(hubResponseContainer))
                .isInstanceOf(HubResponseTranslationException.class)
                .hasMessageContaining("Received unsupported LOA from VSP: ");
    }

    private HubResponseContainer buildHubResponseContainer(Attributes attributes) {
        return new HubResponseContainer(buildHubResponseTranslatorRequest(), new TranslatedHubResponseBuilder().withAttributes(attributes).build());
    }

    private HubResponseContainer buildHubResponseContainer(TranslatedHubResponse translatedHubResponse) {
        return new HubResponseContainer(buildHubResponseTranslatorRequest(), translatedHubResponse);
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
