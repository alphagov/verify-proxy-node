package uk.gov.ida.notification.translator.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Address;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance.LEVEL_2;
import static uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario.IDENTITY_VERIFIED;

public class VspClientRule extends DropwizardClientRule {
    public VspClientRule() {
        super(new StubVspResource());
    }

    @Path("/vsp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class StubVspResource {

        @POST
        @Path(Urls.VerifyServiceProviderUrls.TRANSLATE_HUB_RESPONSE_ENDPOINT)
        public TranslatedHubResponse getTranslatedHubResponse(HubResponseTranslatorRequest hubResponseTranslatorRequest) {
            return new TranslatedHubResponse(IDENTITY_VERIFIED, "123456", LEVEL_2, buildAttributes());
        }

        private Attributes buildAttributes() {
            return new Attributes(
                    new Attribute<>("John", true, LocalDateTime.of(2001, 1, 1, 12, 0), null),
                    null,
                    Collections.singletonList(new Attribute<>("Smith", true, LocalDateTime.of(2001, 1, 1, 12, 0), null)),
                    new Attribute<>(LocalDate.of(1990, 1, 1), true, LocalDateTime.of(2001, 1, 1, 12, 0), null),
                    new Attribute<>("NOT_SPECIFIED", true, LocalDateTime.of(2001, 1, 1, 12, 0), null),
                    Collections.singletonList(new Attribute<>(new Address(Collections.singletonList("1 Acacia Avenue"), "SW1A 1AA", null, null),
                            true, LocalDateTime.of(2001, 1, 1, 12, 0), null)));
        }
    }
}
