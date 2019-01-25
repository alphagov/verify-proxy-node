package uk.gov.ida.notification.stubconnector.resources;

import io.dropwizard.jersey.sessions.Session;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.ldaptive.Message;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.InOutOperationContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.impl.SAMLMetadataLookupHandler;
import org.opensaml.saml.common.binding.security.impl.InResponseToSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.MessageLifetimeSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.MessageReplaySecurityHandler;
import org.opensaml.saml.common.binding.security.impl.ReceivedEndpointSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.SAMLProtocolMessageXMLSignatureSecurityHandler;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.impl.BasicSignatureValidationParametersResolver;
import org.opensaml.xmlsec.messaging.impl.PopulateSignatureValidationParametersHandler;
import se.litsec.eidas.opensaml.ext.attributes.EidasAttributeValueType;
import se.litsec.opensaml.common.validation.CoreValidatorParameters;
import se.litsec.opensaml.xmlsec.SAMLObjectDecrypter;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.stubconnector.BasicMessageHandlerChain;
import uk.gov.ida.notification.stubconnector.EncryptedAssertionCountValidationHandler;
import uk.gov.ida.notification.stubconnector.StripAssertionsHandler;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;
import uk.gov.ida.notification.stubconnector.views.ResponseView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/SAML2/Response")
public class ReceiveResponseResource {
    private final StubConnectorConfiguration configuration;
    private final Metadata proxyNodeMetadata;
    private final SAMLObjectDecrypter decrypter;

    public ReceiveResponseResource(
        StubConnectorConfiguration configuration, Metadata proxyNodeMetadata, SAMLObjectDecrypter decrypter) {
        this.configuration = configuration;
        this.proxyNodeMetadata = proxyNodeMetadata;
        this.decrypter = decrypter;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ResponseView receiveResponse(
        @Session HttpSession session,
        @Context HttpServletRequest httpServletRequest
    ) throws ComponentInitializationException, MessageDecodingException, MessageHandlerException, DecryptionException {

//        SAMLSignatureProfileValidator samlSignatureProfileValidator = new SAMLSignatureProfileValidator();
//        responseValidator = new ResponseValidator(connectorMetadataResolverBundle.getSignatureTrustEngine(), samlSignatureProfileValidator);
//
//        String authnRequestId = (String) session.getAttribute("authn_id");
//
//        ValidationContext validationContext = new ValidationContext(buildStaticParemeters(authnRequestId));
//
//        ValidationResult validate = responseValidator.validate(response, validationContext);
//
//        // The eIDAS Response should only contain one Assertion with one AttributeStatement which contains
//        // the user's requested attributes
//
//        List<String> attributes = new ArrayList<>();
//
//        Response decrypted = decrypter.decrypt(response);
//        List<Assertion> assertions = decrypted.getAssertions();

        HTTPPostDecoder decoder = new HTTPPostDecoder();
        decoder.setHttpServletRequest(httpServletRequest);
        decoder.initialize();
        decoder.decode();

        MessageContext messageContext = decoder.getMessageContext();

        AuthnRequest sessionAuthnRequest = (AuthnRequest) session.getAttribute("authn_request");
        MessageContext sessionRequestContext = new MessageContext();
        sessionRequestContext.setMessage(sessionAuthnRequest);

        InOutOperationContext inOutOperationContext = messageContext.getSubcontext(InOutOperationContext.class);
        inOutOperationContext.setInboundMessageContext(messageContext);
        inOutOperationContext.setOutboundMessageContext(sessionRequestContext);

        SAMLPeerEntityContext senderEntityContext = messageContext.getSubcontext(SAMLPeerEntityContext.class, true);
        senderEntityContext.setRole(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        SAMLMetadataLookupHandler metadataLookupHandler = new SAMLMetadataLookupHandler();
        metadataLookupHandler.setRoleDescriptorResolver(
            proxyNodeMetadata.getMetadataCredentialResolver().getRoleDescriptorResolver()
        );

        PopulateSignatureValidationParametersHandler signatureValidationParametersHandler = new PopulateSignatureValidationParametersHandler();
        signatureValidationParametersHandler.setConfigurationLookupStrategy(msgCtx -> List.of(
            proxyNodeMetadata.getSignatureValidationConfiguration(),
            SecurityConfigurationSupport.getGlobalSignatureValidationConfiguration()
        ));
        signatureValidationParametersHandler.setSignatureValidationParametersResolver(new BasicSignatureValidationParametersResolver());

        SAMLProtocolMessageXMLSignatureSecurityHandler signatureSecurityHandler = new SAMLProtocolMessageXMLSignatureSecurityHandler();
        InResponseToSecurityHandler inResponseToSecurityHandler = new InResponseToSecurityHandler();
        MessageLifetimeSecurityHandler lifetimeSecurityHandler = new MessageLifetimeSecurityHandler();
        lifetimeSecurityHandler.setRequiredRule(true);
        ReceivedEndpointSecurityHandler receivedEndpointSecurityHandler = new ReceivedEndpointSecurityHandler();
        receivedEndpointSecurityHandler.setHttpServletRequest(httpServletRequest);
        StripAssertionsHandler stripAssertionsHandler = new StripAssertionsHandler();
        EncryptedAssertionCountValidationHandler assertionCountValidationHandler = new EncryptedAssertionCountValidationHandler();
        assertionCountValidationHandler.setEncryptedAssertionCount(1);

        BasicMessageHandlerChain handlerChain = new BasicMessageHandlerChain(List.of(
            inResponseToSecurityHandler,
            lifetimeSecurityHandler,
            receivedEndpointSecurityHandler,
            stripAssertionsHandler,
            assertionCountValidationHandler,
            signatureValidationParametersHandler,
            signatureSecurityHandler
        ));

        try {
            handlerChain.invoke(messageContext);
        } catch(MessageHandlerException e) {
            return new ResponseView(Collections.emptyList(), "Message handling failed: " + e.getMessage());
        }

        Response response = (Response) messageContext.getMessage();
        List<Assertion> assertions = new ArrayList<>();

        for (EncryptedAssertion encryptedAssertion : response.getEncryptedAssertions()) {
            assertions.add(decrypter.decrypt(encryptedAssertion, Assertion.class));
        }

        if (assertions.size() > 0) {
            Assertion assertion = assertions.get(0);
            AttributeStatement attributeStatement = assertion.getAttributeStatements().get(0);

            List<String> attributes = attributeStatement
                    .getAttributes()
                    .stream()
                    .map(attr -> ((EidasAttributeValueType) attr.getAttributeValues().get(0)).toStringValue())
                    .collect(Collectors.toList());

            return new ResponseView(attributes, "valid");
        }

        return new ResponseView(Collections.emptyList(), "");
    }

    private Map<String,Object> buildStaticParemeters(String authnRequestId) {
        String responseDestination = configuration.getConnectorNodeBaseUrl() + "/SAML2/Response/POST";

        HashMap<String, Object> params = new HashMap<>();

        params.put(CoreValidatorParameters.SIGNATURE_REQUIRED, true);
        params.put(CoreValidatorParameters.AUTHN_REQUEST_ID, authnRequestId);
        params.put(CoreValidatorParameters.RECEIVE_URL, responseDestination);

        CriteriaSet criteria = new CriteriaSet();
        criteria.add(new EntityIdCriterion(configuration.getProxyNodeEntityId()));
        criteria.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteria.add(new UsageCriterion(UsageType.SIGNING));
        criteria.add(new ProtocolCriterion(SAMLConstants.SAML20P_NS));

        params.put(CoreValidatorParameters.SIGNATURE_VALIDATION_CRITERIA_SET, criteria);


        return params;
    }
}
