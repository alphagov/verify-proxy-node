package uk.gov.ida.notification.apprule.rules;

import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.exceptions.EidasSamlParserResponseException;
import uk.gov.ida.notification.exceptions.FailureResponseGenerationException;
import uk.gov.ida.notification.exceptions.RedisSerializationException;
import uk.gov.ida.notification.exceptions.SessionAlreadyExistsException;
import uk.gov.ida.notification.exceptions.SessionAttributeException;
import uk.gov.ida.notification.exceptions.SessionMissingException;
import uk.gov.ida.notification.exceptions.TranslatorResponseException;
import uk.gov.ida.notification.exceptions.proxy.VerifyServiceProviderRequestException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;


@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class TestExceptionMapperResource {

    public static final String SESSION_ID = "aSessionId";
    public static final String HUB_REQUEST_ID = "aHubRequestId";
    public static final String EIDAS_REQUEST_ID = "anEidasRequestId";

    @GET
    @Path("/SessionAlreadyExistsException")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessionAlreadyExistsException() {
        throw new SessionAlreadyExistsException(SESSION_ID, HUB_REQUEST_ID, EIDAS_REQUEST_ID);
    }

    @GET
    @Path("/SessionAttributeException")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessionAttributeException() {
        throw new SessionAttributeException("This is a message", SESSION_ID, HUB_REQUEST_ID, EIDAS_REQUEST_ID);
    }

    @GET
    @Path("/TranslatorResponseException")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTranslatorResponseException() {
        throw new TranslatorResponseException(ApplicationException.createUnauditedException(ExceptionType.CLIENT_ERROR, UUID.randomUUID()),
                                              SESSION_ID, HUB_REQUEST_ID, EIDAS_REQUEST_ID);
    }

    @GET
    @Path("/SessionMissingException")
    public Response getSessionMissingException() {
        throw new SessionMissingException(SESSION_ID);
    }

    @GET
    @Path("/EidasSamlParserResponseException")
    public Response getEidasSamlParserResponseException() {
        throw new EidasSamlParserResponseException(new Exception("This caused an EidasSamlParserResponseException"),
                                                   SESSION_ID);
    }

    @GET
    @Path("/FailureResponseGenerationException")
    public Response getFailureRepsonseGenerationException() {
        throw new FailureResponseGenerationException(new Exception("This caused a FailureResponseGenerationException"),
                                                     EIDAS_REQUEST_ID);
    }

    @GET
    @Path("/VerifyServiceProviderRequestException")
    public Response getVerifyServiceProviderRequestException() {
        throw new VerifyServiceProviderRequestException(new Exception("This caused a VerifyServiceProviderRequestException"),
                                                        SESSION_ID);
    }

    @GET
    @Path("/RedisSerializationException")
    public Response getRedisSerializationException() {
        throw new RedisSerializationException("This is a message", new Exception("This caused a RedisSerializationException"));
    }

    @GET
    @Path("/NullPointerException")
    public Response getGeneric() {
        throw new NullPointerException();
    }
}
