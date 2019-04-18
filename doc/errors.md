# GOV.UK Verify UK Proxy Node

Work in progress exploration of the current state of Exceptions and ExceptionsMappers to identify the potential unhappy paths and potential for refactoring what's already there to use the ProxyNodeLogger.

## Exceptions

The UK Proxy Node defines the following Exceptions:

### Proxy Node Gateway

EidasSamlParserResponseException
FailureResponseGenerationException
RedisSerializationException
SessionAlreadyExistsException
SessionAttributeException
SessionMissingException
TranslatorResponseException

### Proxy Node Shared

#### authnrequest

AuthnRequestException
EidasAuthnRequestTranslationException
InvalidAuthnRequestException

#### hubresponse

HubResponseException
HubResponseTranslationException
InvalidHubResponseException
ResponseAssertionEncryptionException
ResponseSigningException

#### metadata

ConnectorMetadataException
InvalidMetadataException
MissingMetadataException

#### proxy

VerifyServiceProviderRequestException

#### saml

SamlMarshallingException
SamlParsingException
SamlSigningException
SamlUnmarshallingException

## Exception Mappers

Those exceptions are mapped by Exception Mappers so that a response is generated.

~(Most | All) of the exception mappers extend `uk.gov.ida.notification.exceptions.mappers.ExceptionToErrorPageMapper` which itself extends `javax.ws.rs.ext.ExceptionMapper`.  `ExceptionToErrorPageMapper` will log the exception and provide a response that redirects to a URI provided at construction.~

### Proxy Node Gateway

#### `ExceptionToErrorPageMapper`

The following ExceptionMappers extend `ExceptionToErrorPageMapper`:

    uk.gov.ida.notification.exceptions.mappers
    
        EidasSamlParserResponseExceptionMapper 
        GenericExceptionMapper
        VerifyServiceProviderRequestExceptionMapper
        FailureResponseGenerationExceptionMapper
        SessionMissingExceptionMapper
        
It logs this:

```
LOG.error(format("Error whilst contacting uri [{0}]; logId: {1}; requestId: {2}; sessionId: {3}, issuer: {4}; issueInstant: {5}; message: {6}, cause: {7}",
                 uriInfo.getPath(), logId, getAuthnRequestId(exception), getSessionId(exception), getIssuerId(exception), getIssueInstant(exception), message, cause),
                 exception
         );
```

The default value of the fields are null and the ExceptionMapper that extends it should provide the values.  In practice there are a lot of things that end up being logged as null.

`getIssuerId` doesn't seem to be overridden by any extending ExceptionMappers.

`getIssueInstant` doesn't seem to be overridden by any extending ExceptionMappers.

`logId` is given the value `String.format("%016x", ThreadLocalRandom.current().nextLong());` but it doesn't ever get shown to the user so is redundant.
   
##### `EidasSamlParserResponseExceptionMapper`

This maps `EidasSamlParserResponseException` which is thrown by `uk.gov.ida.notification.proxy.EidasSamlParserProxy.parse`.  If the error is a `uk.gov.ida.common.ExceptionType.CLIENT_ERROR` then the response status will be `BAD_REQUEST`; otherwise it will be `INTERNAL_SERVER_ERROR`.  This will log the `sessionID` from the `HttpSession` given to a `GET` or `POST` to `EidasAuthnRequestResource`.

##### `GenericExceptionMapper`

This the catch-all exception mapper which catches `Exception`.  If this one gets called, something really did go wrong!  The response status is `INTERNAL_SERVER_ERROR`.

##### `VerifyServiceProviderRequestExceptionMapper`

This maps `VerifyServiceProviderRequestException` which is thrown by `VerifyServiceProviderProxy.generateAuthnRequest(String sessionId)`.  The mappers sets the response status to `INTERNAL_SERVER_ERROR`.

This overrides `getSessionId()`.

##### `FailureResponseGenerationExceptionMapper`

This maps `FailureResponseGenerationException` which is thrown by:

`ExceptionToSamlErrorResponseMapper.toResponse(exception)` if there's a problem getting `GatewaySessionData`.

`TranslatorProxy.getSamlErrorResponse(SamlFailureResponseGenerationRequest)` if there's a problem `translatorClient.post(SamlFailureResponseGenerationRequest, failureResponseUri, String.class)`

This overrides `getAuthnRequestId(Exception)`

It sets the responses status to `INTERNAL_SERVER_ERROR`

##### `SessionMissingExceptionMapper`

This maps SessionMissingException.

It doesn't override any of the getters.

It sets the response status to `BAD_REQUEST`.

`InMemoryStorage.getSession(String sessionId)`
`RedisStorage.getSession(String sessionId)`

*refactor above to follow the format below*, which works a bit better than markdown tables.

## `ExceptionToSamlErrorReponseMapper<TException extends Exception> implements ExceptionMapper<TException>`

Provides a SAML form page view as the `Response`.

The form contains `eidasDestination`, JSON containing (`Response.Status`, `eidasRequestId`, `eidasDestination` [sic]) and `eidasRelayState`

The exception logs the following format string:

     Error whilst contacting uri [{0}]; logId: {1}; requestId: {2}; sessionId: {3}, issuer: {4}; issueInstant: {5}; message: {6}, cause: {7}

The class defines two abstract methods:

     Response.Status getResponseStatus(TException exception)
     String getErrorPageMessage(TException exception)
     
`getErrorPageMessage` isn't used according to IntelliJ

The class defines the following methods which all return `null`:

    String getAuthnRequestId(TException exception)
    String getIssuerId(TException exception)
    String getSessionId(TException exception)
    Date getIssueInstant(TException exception)
    
This means that unless an extending class implements one of those methods, the value logged will be `null`.

### Extended by:

####`HubResponseExceptionMapper`

#####Response.Status:

     BAD_REQUEST

#####Overrides / Provides:

     getErrorPageMessage(Exception)

     getAuthnRequestId(Exception)

     getIssuerId(Exception)

     getIssuInstant(Exception)

#####Mapped exception thrown by:

     nothing apparently

---

#### `SessionAlreadyExistsExceptionMapper`

     BAD_REQUEST
     
#####Overrides / Provides:

     nothing

#####Mapped exception thrown by:

     InMemoryStorage#addSession(String sessionId)
     InMemoryStorage#addSession(String sessionId)

---
 
#### `TranslatorResponseExceptionMapper`

#####Response.Status:


#####Overrides / Provides:


#####Mapped exception thrown by:


---
 
#### `SessionAttributeExceptionMapper`

#####Response.Status:


#####Overrides / Provides:


#####Mapped exception thrown by:

---
 









    














