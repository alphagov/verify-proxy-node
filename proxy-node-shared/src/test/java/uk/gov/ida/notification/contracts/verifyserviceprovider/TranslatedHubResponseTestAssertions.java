package uk.gov.ida.notification.contracts.verifyserviceprovider;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.impl.DateOfBirthTypeImpl;
import se.litsec.opensaml.saml2.attribute.AttributeUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TranslatedHubResponseTestAssertions {

    public static void assertAttributes(Response decryptedEidasResponse) {
        org.opensaml.saml.saml2.core.Attribute attribute;
        Assertion eidasAssertion = decryptedEidasResponse.getAssertions().get(0);
        List<Attribute> attributes = eidasAssertion.getAttributeStatements().get(0).getAttributes();

        assertThat(4).isEqualTo(attributes.size());

        attribute = attributes.get(0);
        assertThat("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName").isEqualTo(attribute.getName());
        assertThat("FirstName").isEqualTo(attribute.getFriendlyName());
        assertThat("Jean Paul").isEqualTo(getAttributeValue(attribute));

        attribute = attributes.get(1);
        assertThat("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName").isEqualTo(attribute.getName());
        assertThat("FamilyName").isEqualTo(attribute.getFriendlyName());
        assertThat("Smith").isEqualTo(getAttributeValue(attribute));

        attribute = attributes.get(2);
        assertThat("http://eidas.europa.eu/attributes/naturalperson/DateOfBirth").isEqualTo(attribute.getName());
        assertThat("DateOfBirth").isEqualTo(attribute.getFriendlyName());
        assertThat("1990-01-01").isEqualTo(getAttributeDateValueString(attribute));

        attribute = attributes.get(3);
        assertThat("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier").isEqualTo(attribute.getName());
        assertThat("PersonIdentifier").isEqualTo(attribute.getFriendlyName());
        assertThat("UK/EU/123456").isEqualTo(getAttributeValue(attribute));
    }

    public static void checkAssertionStatementsValid(Response decryptedEidasResponse) {

        Assertion eidasAssertion = decryptedEidasResponse.getAssertions().get(0);

        assertThat(2).isEqualTo(eidasAssertion.getStatements().size());
        assertThat(1).isEqualTo(eidasAssertion.getAttributeStatements().size());
        assertThat(1).isEqualTo(eidasAssertion.getAuthnStatements().size());
        assertThat(EidasConstants.EIDAS_LOA_SUBSTANTIAL)
                .isEqualTo(eidasAssertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef());
    }

    public static void assertResponseForIdentityVerifiedStatus(Response decryptedEidasResponse) {
        assertThat(StatusCode.SUCCESS).isEqualTo(decryptedEidasResponse.getStatus().getStatusCode().getValue());
    }

    public static void assertResponseForCancelledStatus(Response decryptedEidasResponse) {
        assertThat(StatusCode.RESPONDER).isEqualTo(decryptedEidasResponse.getStatus().getStatusCode().getValue());
    }

    public static void assertResponseForAuthenticationFailedStatus(Response decryptedEidasResponse) {
        assertThat(StatusCode.AUTHN_FAILED).isEqualTo(decryptedEidasResponse.getStatus().getStatusCode().getValue());
    }

    private static String getAttributeDateValueString(Attribute attribute) {
        return ((DateOfBirthTypeImpl) attribute.getAttributeValues().get(0)).formatDate();
    }

    private static String getAttributeValue(Attribute attribute) {
        return AttributeUtils.getAttributeStringValue(attribute);
    }
}
