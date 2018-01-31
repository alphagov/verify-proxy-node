package uk.gov.ida.notification.saml.translation;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.test.builders.DateAttributeValueBuilder;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class EidasResponseTranslaterBuilderTest extends SamlInitializedTest {

    private String connectorNodeUrl = "http://connector.eu";
    private String proxyNodeMetadataForConnectorNodeUrl = "http://proxy-node.uk/connector-node-metadata";
    private String connectorNodeIssuerId = "connectorNode issuerId";

    @Mock
    private HubResponseContainer hubResponseContainer;

    @Test
    public void shouldGenerateAnEidasResponse(){
        EidasResponseBuilder eidasResponseBuilder = new EidasResponseBuilder(connectorNodeUrl, proxyNodeMetadataForConnectorNodeUrl, connectorNodeIssuerId);
        DateTime dummyTime = DateTime.now();
        List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();
        List<Attribute> eidasAttributes = eidasAttributeBuilders
                .stream()
                .map(builder -> builder.build(hubResponseContainer))
                .collect(Collectors.toList());
        eidasResponseBuilder.createEidasResponse("success", "pid", EidasConstants.EIDAS_LOA_SUBSTANTIAL,
                eidasAttributes,"",dummyTime, dummyTime, dummyTime );
    }



}

