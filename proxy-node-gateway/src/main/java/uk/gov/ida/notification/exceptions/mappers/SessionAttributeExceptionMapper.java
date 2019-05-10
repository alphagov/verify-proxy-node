package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.exceptions.SessionAttributeException;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.session.storage.SessionStore;

import javax.ws.rs.core.Response;

public class SessionAttributeExceptionMapper extends ExceptionToSamlErrorResponseMapper<SessionAttributeException> {

    public SessionAttributeExceptionMapper(SamlFormViewBuilder samlFormViewBuilder, TranslatorProxy translatorProxy, SessionStore sessionStorage) {
        super(samlFormViewBuilder, translatorProxy, sessionStorage);
    }

    @Override
    protected Response.Status getResponseStatus(SessionAttributeException exception) {
        return Response.Status.BAD_REQUEST;
    }
}
