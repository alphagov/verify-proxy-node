package uk.gov.ida.notification.stubconnector.resources;

import io.dropwizard.jersey.sessions.Session;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.assertion.ValidationContext;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import se.litsec.eidas.opensaml.ext.attributes.EidasAttributeValueType;
import se.litsec.opensaml.common.validation.CoreValidatorParameters;
import se.litsec.opensaml.saml2.common.response.ResponseValidator;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;
import uk.gov.ida.notification.stubconnector.views.ResponseView;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/SAML2/Response")
public class ReceiveResponseResource {
    private final StubConnectorConfiguration configuration;
    private final ResponseAssertionDecrypter decrypter;

    private ResponseValidator responseValidator;

    private MetadataResolverBundle<StubConnectorConfiguration> connectorMetadataResolverBundle;

    public ReceiveResponseResource(
            StubConnectorConfiguration configuration,
            ResponseAssertionDecrypter decrypter,
            MetadataResolverBundle<StubConnectorConfiguration> connectorMetadataResolverBundle) {
        this.configuration = configuration;
        this.decrypter = decrypter;
        this.connectorMetadataResolverBundle = connectorMetadataResolverBundle;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ResponseView receiveResponse(
            @Session HttpSession session,
            @FormParam(SamlFormMessageType.SAML_RESPONSE) Response response,
            @FormParam("RelayState") String relayState) {

        SAMLSignatureProfileValidator samlSignatureProfileValidator = new SAMLSignatureProfileValidator();
        responseValidator = new ResponseValidator(connectorMetadataResolverBundle.getSignatureTrustEngine(), samlSignatureProfileValidator);

        String authnRequestId = (String) session.getAttribute("authn_id");

        ValidationContext validationContext = new ValidationContext(buildStaticParemeters(authnRequestId));

        ValidationResult validate = responseValidator.validate(response, validationContext);

        // The eIDAS Response should only contain one Assertion with one AttributeStatement which contains
        // the user's requested attributes

        List<String> attributes = new ArrayList<>();

        Response decrypted = decrypter.decrypt(response);
        List<Assertion> assertions = decrypted.getAssertions();

        if (assertions.size() > 0) {
            Assertion assertion = assertions.get(0);
            AttributeStatement attributeStatement = assertion.getAttributeStatements().get(0);

            attributes = attributeStatement
                    .getAttributes()
                    .stream()
                    .map(attr -> ((EidasAttributeValueType) attr.getAttributeValues().get(0)).toStringValue())
                    .collect(Collectors.toList());
        }

        return new ResponseView(attributes, validate.toString());
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
