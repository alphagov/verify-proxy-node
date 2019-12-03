package uk.gov.ida.notification.translator.saml;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AttributesBuilder;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseTestAssertions;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingTransliterableAttribute;

import java.net.URI;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.AttributesBuilder.createDateTime;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.AttributesBuilder.createNonMatchingTransliterableAttribute;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder.buildTranslatedHubResponseAuthenticationFailed;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder.buildTranslatedHubResponseCancellation;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerified;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerifiedNoAttributes;
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
            new HubResponseTranslator(EidasResponseBuilder::instance, "Issuer", "connectorMetadataURL", "NATIONALITY_CODE");

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
        final Response cancelledResponse = TRANSLATOR.getTranslatedHubResponse(hubResponseContainer);

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(cancelledResponse);
        TranslatedHubResponseTestAssertions.checkResponseStatusCodeValidForCancelledStatus(cancelledResponse);
    }

    @Test
    public void translateShouldReturnResponseForAuthenticationFailedStatus() {
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(buildTranslatedHubResponseAuthenticationFailed());
        final Response authnFailedResponse = TRANSLATOR.getTranslatedHubResponse(hubResponseContainer);

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(authnFailedResponse);
        TranslatedHubResponseTestAssertions.checkResponseStatusCodeValidForAuthenticationFailedStatus(authnFailedResponse);
    }

    @Test
    public void translateShouldThrowIfIdentityVerifiedButNoAttributes() {
        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(buildTranslatedHubResponseIdentityVerifiedNoAttributes());

        assertThatThrownBy(() -> TRANSLATOR.getTranslatedHubResponse(hubResponseContainer))
                .isInstanceOf(HubResponseTranslationException.class)
                .hasMessageContaining("Attributes are null for VSP scenario: IDENTITY_VERIFIED");
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
        final LocalDate validTo = createDateTime(2018, 1, 1);
        final NonMatchingTransliterableAttribute firstName = createNonMatchingTransliterableAttribute(
                AttributesBuilder.FIRST_NAME,
                true,
                AttributesBuilder.VALID_FROM,
                validTo);

        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.withFirstName(firstName).build());

        assertThatThrownBy(() -> TRANSLATOR.getTranslatedHubResponse(hubResponseContainer))
                .isInstanceOf(HubResponseTranslationException.class)
                .hasMessageContaining("No verified current first name present");
    }

    @Test
    public void translateShouldReturnResponseWhenCurrentFirstNameNotVerifiedButNoFromOrToMakesItCurrent() {
        final NonMatchingTransliterableAttribute firstName = createNonMatchingTransliterableAttribute(
                AttributesBuilder.FIRST_NAME,
                false,
                null,
                null);

        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.withFirstName(firstName).build());
        final Attributes attributes = hubResponseContainer.getAttributes().orElseThrow();

        assertThat(attributes.getFirstNamesAttributesList().getValidAttributes().size()).isEqualTo(1);
    }

    @Test
    public void translateShouldReturnResponseWhenCurrentFirstNameNotValidButCurrentFrom() {
        final NonMatchingTransliterableAttribute firstName = createNonMatchingTransliterableAttribute(
                AttributesBuilder.FIRST_NAME,
                false,
                AttributesBuilder.VALID_FROM,
                null);

        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.withFirstName(firstName).build());
        final Attributes attributes = hubResponseContainer.getAttributes().orElseThrow();

        assertThat(attributes.getFirstNamesAttributesList().getValidAttributes().size()).isEqualTo(1);
    }

    @Test
    public void translateShouldReturnResponseWhenCurrentFirstNameNotValidButCurrentTo() {
        final NonMatchingTransliterableAttribute firstName = createNonMatchingTransliterableAttribute(
                AttributesBuilder.FIRST_NAME,
                false,
                null,
                LocalDate.now().plusDays(1));

        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.withFirstName(firstName).build());
        final Attributes attributes = hubResponseContainer.getAttributes().orElseThrow();

        assertThat(attributes.getFirstNamesAttributesList().getValidAttributes().size()).isEqualTo(1);
    }

    @Test
    public void translateShouldIgnoreExpiredExtraNames() {
        final LocalDate validFrom = AttributesBuilder.VALID_FROM.minusYears(3);
        final LocalDate validTo = AttributesBuilder.VALID_FROM;
        final NonMatchingTransliterableAttribute firstName = createNonMatchingTransliterableAttribute(
                "Expired",
                true,
                validFrom,
                validTo);

        final HubResponseContainer hubResponseContainer = buildHubResponseContainer(attributesBuilder.addFirstName(firstName).build());
        final Response response = TRANSLATOR.getTranslatedHubResponse(hubResponseContainer);

        TranslatedHubResponseTestAssertions.checkAssertionStatementsValid(response);
        TranslatedHubResponseTestAssertions.checkAllAttributesValid(response);
        TranslatedHubResponseTestAssertions.checkResponseStatusCodeValidForIdentityVerifiedStatus(response);
    }

    @Test
    public void translateShouldIgnoreUnverifiedExtraNames() {
        final NonMatchingTransliterableAttribute firstName = createNonMatchingTransliterableAttribute(
                "Unverified",
                false,
                AttributesBuilder.VALID_FROM,
                null);

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

    private HubResponseContainer buildHubResponseContainer(NonMatchingAttributes attributes) {
        return new HubResponseContainer(buildHubResponseTranslatorRequest(), new TranslatedHubResponseBuilder().withAttributes(attributes).build());
    }

    private HubResponseContainer buildHubResponseContainer(TranslatedHubResponse translatedHubResponse) {
        return new HubResponseContainer(buildHubResponseTranslatorRequest(), translatedHubResponse);
    }

    private HubResponseTranslatorRequest buildHubResponseTranslatorRequest() {
        return new HubResponseTranslatorRequest(
                "",
                "_request-id_of-20-chars-or-more",
                ResponseBuilder.DEFAULT_REQUEST_ID,
                "LEVEL_2",
                URI.create("http://localhost:8081/bob"),
                STUB_COUNTRY_PUBLIC_PRIMARY_CERT);
    }
}
