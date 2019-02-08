package uk.gov.ida.notification.exceptions.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;

import static java.text.MessageFormat.format;

public abstract class BaseExceptionMapper<TException extends Exception> implements ExceptionMapper<TException> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseExceptionMapper.class);

    private UriInfo uriInfo;

    private HttpServletRequest httpServletRequest;

    @Context
    public void setUriInfo(UriInfo uriInfo){
        this.uriInfo = uriInfo;
    }

    @Context
    public void setHttpServletRequest(HttpServletRequest httpServletRequest){
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Response toResponse(TException exception) {
        LOG.error(format("Exception whilst contacting uri [{0}]: {1}", uriInfo.getPath(), exception.getMessage()), exception);
        return handleException(exception);
    }

    protected abstract Response handleException(TException exception);
}
