package uk.gov.ida.notification.apprule.rules;

import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.shared.Urls;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_DESTINATION_URL;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_EIDAS_REQUEST_ID;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;

@Path(Urls.EidasSamlParserUrls.EIDAS_AUTHN_REQUEST_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class TestEidasSamlResource {

    @POST
    @Valid
    public EidasSamlParserResponse post(@Valid EidasSamlParserRequest request) {
        return new EidasSamlParserResponse(
                SAMPLE_EIDAS_REQUEST_ID,
                SAMPLE_ENTITY_ID,
                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                SAMPLE_DESTINATION_URL
        );
    }
}
