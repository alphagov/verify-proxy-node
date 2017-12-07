package uk.gov.ida.notification;

import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.translation.HubResponse;
import uk.gov.ida.notification.saml.translation.HubResponseTranslator;

public class EidasResponseGenerator {

    private HubResponseTranslator hubResponseTranslator;

    public EidasResponseGenerator(HubResponseTranslator hubResponseTranslator) {

        this.hubResponseTranslator = hubResponseTranslator;
    }

    public Response generate(HubResponse hubResponse) throws Throwable {
        return hubResponseTranslator.translate(hubResponse);
    }
}
