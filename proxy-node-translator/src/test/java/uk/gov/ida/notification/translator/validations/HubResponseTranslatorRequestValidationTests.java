package uk.gov.ida.notification.translator.validations;

import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.validations.AbstractDtoValidationsTest;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;

import javax.validation.ConstraintViolation;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.ida.notification.validations.ValidationTestDataUtils.sample_connectorEncryptionCertificate;
import static uk.gov.ida.notification.validations.ValidationTestDataUtils.sample_destinationUrl;
import static uk.gov.ida.notification.validations.ValidationTestDataUtils.sample_eidasRequestId;
import static uk.gov.ida.notification.validations.ValidationTestDataUtils.sample_levelofAssurance;
import static uk.gov.ida.notification.validations.ValidationTestDataUtils.sample_requestId;
import static uk.gov.ida.notification.validations.ValidationTestDataUtils.sample_samlResponse;

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
    public void allNullParametersShouldFailValidation() {
        HubResponseTranslatorRequest nullRequest = new HubResponseTranslatorRequest(
                null,
                null,
                null,
                null,
                null,
                null);

        Map<String,List<ConstraintViolation<HubResponseTranslatorRequest>>> nullViolationsMap = validateAndMap(nullRequest);
        assertEquals("Null parameters in request should all fail.", 6, nullViolationsMap.size());
    }

    @Test
    public void allValidParametersShouldPassValidation() {
        HubResponseTranslatorRequest goodRequest = new HubResponseTranslatorRequest(
                sample_samlResponse,
                sample_requestId,
                sample_eidasRequestId,
                sample_levelofAssurance,
                URI.create(sample_destinationUrl),
                sample_connectorEncryptionCertificate);

        Map<String, List<ConstraintViolation<HubResponseTranslatorRequest>>> goodViolationsMap = validateAndMap(goodRequest);

        assertEquals("Good parameters should all pass validation.", 0, goodViolationsMap.size());
    }

    @Test
    public void allInvalidParametersShouldFailValidation() {
        HubResponseTranslatorRequest badRequest = new HubResponseTranslatorRequest(
                "not base 64 SAML",
                "1_should_fail_because_of_the_first_numeric_character",
                "_2_is_too_short",
                "LEVEL_7",
                URI.create("xyz://something.somewhere/with/an/invalid/protocol"),
                "not a certificate");

        Map<String,List<ConstraintViolation<HubResponseTranslatorRequest>>> badViolationsMap = validateAndMap(badRequest);

        assertEquals("Invalid parameters should fail validation.", 6, badViolationsMap.size());
    }

}
