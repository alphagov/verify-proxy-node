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

## `ExceptionToErrorPageMapper`

Provides a redirect response to a URI set at construction.

Logs the following message:

```
LOG.error(format("Error whilst contacting uri [{0}]; logId: {1}; requestId: {2}; sessionId: {3}, issuer: {4}; issueInstant: {5}; message: {6}, cause: {7}",
                 uriInfo.getPath(), logId, getAuthnRequestId(exception), getSessionId(exception), getIssuerId(exception), getIssueInstant(exception), message, cause),
                 exception
         );
```

The class defines the following methods which all return null:

    protected String getAuthnRequestId(TException exception)
    protected String getIssuerId(TException exception)
    protected String getSessionId(TException exception)
    protected DateTime getIssueInstant(TException exception)

So unless an extending class overrides the method, the default value of the fields logged will be null.  In practice there are a lot of things that end up being logged as null.

`logId` is never set so is always `null` in the logged message.


### Extended by:

####`EidasSamlParserResponseExceptionMapper`

#####Response.Status:

Returns `BAD_REQUEST` for `ExceptionType.CLIENT_ERROR` and `INTERNAL_SERVER_ERROR` for anything else.

#####Overrides / Provides:

     getSessionId()

#####Mapped exception thrown by:

     EidasSamlParserProxy#parse(EidasSamlParserRequest eidasSamlParserRequest, String sessionId)

---

####`GenericExceptionMapper`

#####Response.Status:
`INTERNAL_SERVER_ERROR`

#####Overrides / Provides:

     Nothing

#####Mapped exception thrown by:

    SessionRedisCodec#decodeValue(ByteBuffer bytes) as RedisSerializationException
    SessionRedisCodec#encodeValue(ByteBuffer bytes) as RedisSerializationException
        

    Anything that isn't mapped by anything else.  This is for when something really did go wrong!   

---

####`VerifyServiceProviderRequestExceptionMapper`

#####Response.Status:
`INTERNAL_SERVER_ERROR`

#####Overrides / Provides:

     getSessionId()

#####Mapped exception thrown by:

     VerifyServiceProviderProxy#generateAuthnRequest(String sessionId)

---

####`FailureResponseGenerationExceptionMapper`

#####Response.Status:
`INTERNAL_SERVER_ERROR`

#####Overrides / Provides:

     getAuthnRequestId(Exception)

#####Mapped exception thrown by:

`ExceptionToSamlErrorResponseMapper.toResponse(exception)` if there's a problem getting `GatewaySessionData`.

`TranslatorProxy.getSamlErrorResponse(SamlFailureResponseGenerationRequest)` if there's a problem `translatorClient.post(SamlFailureResponseGenerationRequest, failureResponseUri, String.class)`     

---

####`SessionMissingExceptionMapper`

#####Response.Status:
`BAD_REQUEST`

#####Overrides / Provides:

     Nothing

#####Mapped exception thrown by:

     InMemoryStorage#getSession(String sessionId)
     RedisStorage#getSession(String sessionId)

---

## `ExceptionToSamlErrorReponseMapper`

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
     RedisStorage#addSession(String sessionId)

---
 
#### `TranslatorResponseExceptionMapper`

#####Response.Status:

Returns `BAD_REQUEST` for `ExceptionType.CLIENT_ERROR` and `INTERNAL_SERVER_ERROR` for anything else.

#####Overrides / Provides:

    getErrorPageMessage(Exception)
    getSessionId(Exception)

#####Mapped exception thrown by:

        TranslatorProxy#TranslatorResponseException(Throwable cause, String sessionId)

---
 
#### `SessionAttributeExceptionMapper`

#####Response.Status:

    BAD_REQUEST

#####Overrides / Provides:

    getErrorPageMessage(Exception)

#####Mapped exception thrown by:

    GatewaySessionData#validate()
    
---

---

 
##proxy-node-shared

##BaseExceptionMapper

Contains a `UriInfo` field which is used in logging but the setter seems to be unused.

Overrides `ExceptionMapper#toResponse(exception)` which:
 
Creates a logId `String.format("%016x", ThreadLocalRandom.current().nextLong())`

Logs the exception:

```java
        LOG.error(format("Error whilst contacting uri [{0}]; logId: {1}; requestId: {2}; issuer: {3}; issueInstant: {4}; message: {5}; cause: {6}",
                uriInfo.getPath(), logId, getAuthnRequestId(exception), getIssuerId(exception), getIssueInstant(exception), message, cause),
                exception
        );
```
 
and returns a response as JSON object representing and `ErrorMessage` containing the `Response.Status` statusCode and a response message.

Has the following abstract methods:

    protected abstract Response.Status getResponseStatus();
    protected abstract String getResponseMessage(TException exception);

Has the following protected methods:

```java
    protected String getAuthnRequestId(TException exception) {
        return null;
    }

    protected String getIssuerId(TException exception) {
        return null;
    }

    protected DateTime getIssueInstant(TException exception) {
        return null;
    }
```

###ExtendedBy:

####ApplicationExceptionMapper

#####Response.Status:

    INTERNAL_SERVER_ERROR

#####Overrides / Provides:

```java
    @Override
    protected String getResponseMessage(ApplicationException exception) {
        return format("Exception with id {0} of type {1} whilst contacting uri [{2}]: {3}",
                exception.getErrorId(), exception.getExceptionType(), exception.getUri(), exception.getMessage());
    }
```    

#####Mapped exception thrown by:

    proxy-node-translator/TranslatorApplication

---

####`AuthnRequestExceptionMapper`

#####Response.Status:

    BAD_REQUEST
    
#####Override / Provides:

    getResponseMessage(exception) { return "Error handling authn request. logId: " + getLogId(); }
    getAuthnRequestId(exception)  { return exception.getAuthnRequest().getID(); }
    getIssuerId(AuthnRequestException exception) { return exception.getAuthnRequest().getIssuer().getValue(); }
    getIssueInstant(AuthnRequestException exception) { return exception.getAuthnRequest().getIssueInstant(); }
    
#####Mapped exception thrown by:

Nothing apparently

####`HubResponseExceptionMapper`

#####Mapped exception thrown by:

Nothing apparently.

---

####`HubResponseTranslationExceptionMapper`

#####Response.Status:

    BAD_REQUEST
    
#####Overrides / Provides

```java
@Override
    protected String getResponseMessage(HubResponseTranslationException exception) {
        return format("Error whilst handling hub response: {0}; {1}", exception.getMessage(), exception.getCause().getMessage());
    }
```

#####Mapped exception thrown by:

`HubResponseTranslator#getMappedLoa(VspLevelOfAssurance vspLoa)` if LOA is not LEVEL_2

`HubResponseTranslator#getMappedStatusCode(VspScenario vspScenario)` if vspScenario is not IDENTITY_VERIFIED, CANCELLATION, AUTHENTICATION_FAILED or is REQUEST_ERROR

---

####`InvalidAuthnRequestExceptionMapper`

##### Response.Status:

    BAD_REQUEST
    
##### Overrides / Provides
```java
    @Override
    protected String getResponseMessage(InvalidAuthnRequestException exception) {
        return format("ESP InvalidAuthnRequestException: {0}.", exception.getMessage());
    }
```

Where exception.getMessage() will be `"Bad Authn Request from Connector Node: "` plus the message supplied below.

##### Mapped exception thrown by:

`EiadasAuthnRequestValidator#validate(AuthnRequest)`

If:

```java
        if (request == null) {
            throw new InvalidAuthnRequestException("Null request");
        }

        if (Strings.isNullOrEmpty(request.getID())) {
            throw new InvalidAuthnRequestException("Missing Request ID");
        }

        if (request.getExtensions() == null) {
            throw new InvalidAuthnRequestException("Missing Extensions");
        }

        if (request.isPassive()) {
            throw new InvalidAuthnRequestException("Request should not require zero user interaction (isPassive should be missing or false)");
        }

        if (!request.isForceAuthn()) {
            throw new InvalidAuthnRequestException("Request should require fresh authentication (forceAuthn should be true)");
        }

        if (!Strings.isNullOrEmpty(request.getProtocolBinding())) {
            throw new InvalidAuthnRequestException("Request should not specify protocol binding");
        }

        if (request.getVersion() != SAMLVersion.VERSION_20) {
            throw new InvalidAuthnRequestException("SAML Version should be " + SAMLVersion.VERSION_20.toString());
        }

```

and if:

`MessageReplayChecker#checkReplay(request.getID)` throws `MessageReplayException` or `DestinationValidator#validate(String destination)` throws `SamlValidationException`

In which case the appended message will be the message from the underlying exception and the underlying exception will be logged by `BaseExceptionMapper`

(`SamlValidationException` is in the package `uk.gov.ida.notification.saml.deprecate`)

---

`AssertionConsumerServiceValidator#validate(AuthnRequest)`

if the String `assertionConsumerServiceURL` is not either null or empty;

or locationsFromMetadata.stream() does not contain `assertionConsumerServiceURL`

```
new InvalidAuthnRequestException("Supplied AssertionConsumerServiceURL has no match in metadata. " +
                String.format("Supplied: %s. In metadata: %s.", assertionConsumerServiceURL, String.join(", ", locationsFromMetadata)));
```

---

`ComparisonValidator#validate(RequestedAuthnContext)`

if the authnContext is `null`:

```
new InvalidAuthnRequestException("Request has no requested authentication context");
```

or the `AuthnContextComparisonTypeEnumeration` contained in the authnContext is not `AuthnContextComparisonTypeEnumeration.MINIMUM`

`new InvalidAuthnRequestException("Comparison type, if present, must be " + validType.toString());`

---

`RequestedAttributesValidator#validate(RequestedAttributes requestedAttributesParent)`

if `requestedAttributesParent` is null

```java
new InvalidAuthnRequestException("Missing RequestedAttributes")
```

if any mandatory attributes are missing:

```java
new InvalidAuthnRequestException(MessageFormat.format("Missing mandatory RequestedAttribute(s): {0}", String.join(", ", missingMandatoryAttributes)));
```

or if any of the requested attributes fail validation by the private method `RequestedAttribute validateRequestedAttribute(RequestedAttribute requestedAttribute)`

if the name format doesn't match the URI `"urn:oasis:names:tc:SAML:2.0:attrname-format:uri"`

`new ... MessageFormat.format("Invalid RequestedAttribute NameFormat ''{0}''", requestedAttribute.getNameFormat());`

if the attribute is in the list of mandatory attributes but the attribute's isRequired() method returns false

`new ... MessageFormat.format("Mandatory RequestedAttribute needs to be required ''{0}''", requestedAttribute.getName());`

if the attribute is in the list of mandatory attributes and the attribute's isRequired() method returns true.

`new ... MessageFormat.format("Non-mandatory RequestedAttribute should not be required ''{0}''", requestedAttribute.getName())`

---

`RequestIssuerValidator#validate(Issuer)`

if issuer.getValue() is null or empty

`new InvalidAuthnRequestException("Missing Issuer")`

---

`SpTypeValidator#validate(SPType)`

if spType is not `PUBLIC`

`InvalidAuthnRequestException(MessageFormat.format("Invalid SPType ''{0}''", spTypeType))`

___

`LoaValidator#validate(RequestedAuthnContext)`

if requestedAuthnContext is `null`

InvalidAuthnRequestException("Missing RequestedAuthnContext")

or there's no LoA

`new InvalidAuthnRequestException("Missing LoA")`

if the LoA is not `EIDAS_LOA_SUBSTANTIAL` or not `EIDAS_LOA_LOW`

`InvalidAuthnRequestException(MessageFormat.format("Invalid LoA ''{0}''", loa))`

---

####`ResponseSigningExceptionMapper`

#####Response.Status:

    INTERNAL_SERVER_ERROR
    
#####Overrides / Provides

```java
    @Override
    protected String getResponseMessage(ResponseSigningException exception) {
        return MessageFormat.format("{0}; {1}", exception.getMessage(), exception.getCause().getMessage());
    }
```

#####Mapped exception thrown by:

`EidasResponseGenerator#signSamlResponse(Response, String eidasRequestId)`

If `samlObjectSigner.sign(Response, String eidasRequestId)` throws `MarshallingException`, `SecurityException` or `SignatureException`

---

####`SamlTransformationErrorExceptionMapper`

#####Response.Status:

    BAD_REQUEST
    
#####Overrides / Provides

```java
    @Override
    protected String getResponseMessage(SamlTransformationErrorException exception) {
        return format("Error during AuthnRequest Signature Validation: {0};", exception.getMessage());
    }
```

#####Mapped exception thrown by:

`ElementToOpenSamlXMLObjectTransformer#apply(Element input)` if `samlObjectParser.getSamleObject(input)` throws an `UnmarshallingException`. This will provide a log level.

`OpenSamlXMLObjectUnmarshaller#fromString(String input)` if `samlObjectParser.getSamleObject(input)` throws an `UnmarshallingException` or `XMLParserException`. This will provide a log level.

`SamlAssertionsSignatureValidator#validate(List<Assertion>, QName)` if the signature does not validate.



