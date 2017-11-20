package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

public class HubToEidasResponseTransformer {

    public Response transform(Response hubResponse) {
        Response eidasResponse = (Response) XMLObjectSupport.buildXMLObject(Response.DEFAULT_ELEMENT_NAME);

        eidasResponse.setStatus(mapStatus(hubResponse.getStatus()));

        return eidasResponse;
    }

    private Status mapStatus(Status hubStatus) {
        Status status = SamlBuilder.build(Status.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = SamlBuilder.build(StatusCode.DEFAULT_ELEMENT_NAME);

        statusCode.setValue(hubStatus.getStatusCode().getValue());
        status.setStatusCode(statusCode);

        return status;
    }
}
