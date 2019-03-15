package uk.gov.ida.notification.exceptions.mappers;

import org.joda.time.DateTime;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseException;

import javax.ws.rs.core.Response;
import java.text.MessageFormat;

public class HubResponseExceptionMapper extends BaseExceptionMapper<HubResponseException> {

    @Override
    protected Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }

    @Override
    protected String getResponseMessage(HubResponseException exception) {
        return MessageFormat.format("Error handling hub response. logId: {0}", getLogId());
    }

    @Override
    protected String getAuthnRequestId(HubResponseException exception) {
        return exception.getSamlResponse().getID();
    }

    @Override
    protected String getIssuerId(HubResponseException exception) {
        return exception.getSamlResponse().getIssuer().getValue();
    }

    @Override
    protected DateTime getIssueInstant(HubResponseException exception) {
        return exception.getSamlResponse().getIssueInstant();
    }
}
