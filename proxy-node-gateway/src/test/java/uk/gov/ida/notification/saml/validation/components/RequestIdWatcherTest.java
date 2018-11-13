package uk.gov.ida.notification.saml.validation.components;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.ida.notification.helpers.HubResponseBuilder.aHubResponse;
import static uk.gov.ida.saml.core.test.builders.AuthnRequestBuilder.anAuthnRequest;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.support.SignatureException;

import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

public class RequestIdWatcherTest {
  private RequestIdWatcher requestIdWatcher;

  @Before
  public void setUp() throws InitializationException {
    InitializationService.initialize();
    this.requestIdWatcher = new RequestIdWatcher();
  }

  @Test
  public void shouldNotValidateIdThatHasNotBeenObserved() throws MarshallingException, SignatureException {
    Response response = aHubResponse().withInResponseTo(ResponseBuilder.DEFAULT_REQUEST_ID).build();

    assertFalse(this.requestIdWatcher.haveSeenRequestFor(response));
    assertThrows(InvalidHubResponseException.class, () -> this.requestIdWatcher.validateSeenRequestFor(response));
  }

  @Test
  public void shouldValidateIdThatHasBeenObserved() throws MarshallingException, SignatureException {
    AuthnRequest authnRequest = anAuthnRequest().build();
    Response response = aHubResponse().withInResponseTo(authnRequest.getID()).build();

    this.requestIdWatcher.observe(authnRequest);
    assertTrue(this.requestIdWatcher.haveSeenRequestFor(response));
    this.requestIdWatcher.validateSeenRequestFor(response);
  }

  @Test
  public void shouldValidateManyIdsThatHaveBeenObserved() throws MarshallingException, SignatureException {
    for (AuthnRequest authnRequest : Stream.generate(() -> anAuthnRequest().build()).limit(128).collect(toList())) {
      this.requestIdWatcher.observe(authnRequest);
      Response response = aHubResponse().withInResponseTo(authnRequest.getID()).build();

      assertTrue(this.requestIdWatcher.haveSeenRequestFor(response));
      this.requestIdWatcher.validateSeenRequestFor(response);
    }
  }
}