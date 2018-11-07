package uk.gov.ida.notification.stubconnector.resources;

import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.stubconnector.EidasAuthnRequestGenerator;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.List;


@Path("/Request")
public class SendAuthnRequestResource {
    private final StubConnectorConfiguration configuration;
    private final Metadata proxyNodeMetadata;
    private final EidasAuthnRequestGenerator authnRequestGenerator;
    private final SamlFormViewBuilder samlFormViewBuilder;

    public SendAuthnRequestResource(StubConnectorConfiguration configuration, Metadata proxyNodeMetadata, EidasAuthnRequestGenerator authnRequestGenerator, SamlFormViewBuilder samlFormViewBuilder) {
        this.configuration = configuration;
        this.proxyNodeMetadata = proxyNodeMetadata;
        this.authnRequestGenerator = authnRequestGenerator;
        this.samlFormViewBuilder = samlFormViewBuilder;
    }

    @GET
    public SamlFormView setupAuthnRequest() throws ResolverException {
        String proxyNodeEntityId = configuration.getProxyNodeMetadataConfiguration().getExpectedEntityId();
        String ssoUrl = proxyNodeMetadata.getSsoUrl(proxyNodeEntityId);
        String connectorEntityId = configuration.getConnectorNodeBaseUrl() + "/Metadata";
        List<String> requestedAttributes = Arrays.asList(
                AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME
        );
        AuthnRequest authnRequest = authnRequestGenerator.generate(
                UUID.randomUUID().toString(),
                ssoUrl,
                connectorEntityId,
                SPTypeEnumeration.PUBLIC,
                requestedAttributes,
                EidasLoaEnum.LOA_SUBSTANTIAL);
        return samlFormViewBuilder.buildRequest(ssoUrl, authnRequest, "Submit to Proxy Node", "relay");
    }
}
