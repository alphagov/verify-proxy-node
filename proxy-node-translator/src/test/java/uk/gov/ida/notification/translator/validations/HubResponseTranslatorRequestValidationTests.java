package uk.gov.ida.notification.translator.validations;

import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.validations.AbstractDtoValidationsTest;

import javax.validation.ConstraintViolation;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_CONNECTOR_ENCRYPTION_CERTIFICATE;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_DESTINATION_URL;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_EIDAS_REQUEST_ID;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMLPLE_HUB_SAML_RESPONSE;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_LEVEL_OF_ASSURANCE;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_REQUEST_ID;

public class HubResponseTranslatorRequestValidationTests extends AbstractDtoValidationsTest<HubResponseTranslatorRequest> {

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldFailValidationWithAllNullParameters() {
        HubResponseTranslatorRequest nullRequest = new HubResponseTranslatorRequest(
                null,
                null,
                null,
                null,
                null,
                null);

        Map<String, List<ConstraintViolation<HubResponseTranslatorRequest>>> nullViolationsMap = validateAndMap(nullRequest);

        assertThat(nullViolationsMap.size()).isEqualTo(6);
    }

    @Test
    public void shouldPassValidationWithValidParameters() {
        HubResponseTranslatorRequest goodRequest = new HubResponseTranslatorRequest(
                SAMLPLE_HUB_SAML_RESPONSE,
                SAMPLE_REQUEST_ID,
                SAMPLE_EIDAS_REQUEST_ID,
                SAMPLE_LEVEL_OF_ASSURANCE,
                URI.create(SAMPLE_DESTINATION_URL),
                SAMPLE_CONNECTOR_ENCRYPTION_CERTIFICATE);

        Map<String, List<ConstraintViolation<HubResponseTranslatorRequest>>> goodViolationsMap = validateAndMap(goodRequest);

        assertThat(goodViolationsMap.size()).isEqualTo(0);
    }

    @Test
    public void shouldFailValidationWithInvalidParameters() {
        HubResponseTranslatorRequest badRequest = new HubResponseTranslatorRequest(
                "not base 64 SAML",
                "1_should_fail_because_of_the_first_numeric_character",
                "_2_is_too_short",
                "LEVEL_7",
                URI.create("xyz://something.somewhere/with/an/invalid/protocol"),
                "not a certificate");

        Map<String, List<ConstraintViolation<HubResponseTranslatorRequest>>> badViolationsMap = validateAndMap(badRequest);

        assertThat(badViolationsMap.size()).isEqualTo(6);
    }
}
