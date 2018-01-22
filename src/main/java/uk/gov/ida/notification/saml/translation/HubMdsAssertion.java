package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import uk.gov.ida.notification.exceptions.HubResponseException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HubMdsAssertion {
    private final Map<String, AttributeValue> mdsAttributes;
    private final DateTime issueInstant;

    public HubMdsAssertion(Map<String, AttributeValue> mdsAttributes, DateTime issueInstant) {
        this.mdsAttributes = mdsAttributes;
        this.issueInstant = issueInstant;
    }


    public static HubMdsAssertion fromAssertions(List<Assertion> assertions) {
        DateTime issueInstant = assertions.get(0).getIssueInstant();

        Assertion mdsAssertion = assertions
                .stream()
                .filter(a -> a.getAuthnStatements().isEmpty() && !a.getAttributeStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new HubResponseException("Hub Response has no MDS assertion"));

        Map<String, AttributeValue> mdsAttributes = mdsAssertion.getAttributeStatements().get(0).getAttributes().stream()
                .collect(Collectors.toMap(
                        Attribute::getName,
                        a -> (AttributeValue) a.getAttributeValues().get(0)));

        return new HubMdsAssertion(mdsAttributes, issueInstant);
    }

    public <T extends AttributeValue> T getMdsAttribute(String key, Class<T> castClazz) {
        return castClazz.cast(mdsAttributes.get(key));
    }

    public DateTime getIssueInstant() {
        return issueInstant;
    }
}
