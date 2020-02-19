package uk.gov.ida.notification.validations;

import org.junit.Test;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.helpers.ValidationTestDataUtils;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SamlFailureResponseGenerationRequestValidationTests extends AbstractDtoValidationsTest<SamlFailureResponseGenerationRequest> {

    @Test
    public void shouldFailValidationWithAllNullParameters() {
        SamlFailureResponseGenerationRequest nullRequest = new SamlFailureResponseGenerationRequest(
                null,
                null,
                null,
                null);

        Map<String, List<ConstraintViolation<SamlFailureResponseGenerationRequest>>> nullViolationsMap = validateAndMap(nullRequest);

        assertThat(nullViolationsMap.size()).isEqualTo(4);
    }

    @Test
    public void shouldPassValidationWithValidParameters() {
        SamlFailureResponseGenerationRequest goodRequest = new SamlFailureResponseGenerationRequest(
                Response.Status.OK, // null is the only 'malformed' Response.Status
                ValidationTestDataUtils.SAMPLE_EIDAS_REQUEST_ID,
                ValidationTestDataUtils.SAMPLE_DESTINATION_URL,
                URI.create(ValidationTestDataUtils.SAMPLE_ENTITY_ID)
        );

        Map<String, List<ConstraintViolation<SamlFailureResponseGenerationRequest>>> goodViolationsMap = validateAndMap(goodRequest);

        assertThat(goodViolationsMap.size()).isEqualTo(0);
    }

    @Test
    public void shouldFailValidationWithInvalidParameters() {
        // NB. null is the only 'malformed' Response.Status.
        // Even error statuses are accepted by validation and if they indicate problems they are handled elsewhere.
        SamlFailureResponseGenerationRequest badRequest = new SamlFailureResponseGenerationRequest(
                null,
                "SAML Ids cannot have spaces",
                "ftp://that.is/not/a/good/protocol",
                null
        );

        Map<String, List<ConstraintViolation<SamlFailureResponseGenerationRequest>>> badViolationsMap = validateAndMap(badRequest);

        assertThat(badViolationsMap.size()).isEqualTo(4);
    }
}
