package uk.gov.ida.notification;

import uk.gov.ida.notification.dto.VSPAuthnRequestGenerationBody;
import uk.gov.ida.notification.dto.VSPAuthnRequestResponse;

public class VSPService {

    public VSPAuthnRequestResponse generateAuthnRequest() {
        VSPAuthnRequestGenerationBody request = new VSPAuthnRequestGenerationBody("LEVEL_2");
        return new VSPAuthnRequestResponse();
    }
}
