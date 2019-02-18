package uk.gov.ida.notification.contracts.verifyserviceprovider;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.impl.DateOfBirthTypeImpl;
import se.litsec.opensaml.saml2.attribute.AttributeUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TranslatedHubResponseTestAssertions {

    public static void assertAttributes(Response decryptedEidasResponse) {
        org.opensaml.saml.saml2.core.Attribute attribute;
        Assertion eidasAssertion = decryptedEidasResponse.getAssertions().get(0);
        List<Attribute> attributes = eidasAssertion.getAttributeStatements().get(0).getAttributes();

        assertEquals(4, attributes.size());

        attribute = attributes.get(0);
        assertEquals("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName", attribute.getName());
        assertEquals("FirstName", attribute.getFriendlyName());
        assertEquals("Jean Paul", getAttributeValue(attribute));

        attribute = attributes.get(1);
        assertEquals("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName", attribute.getName());
        assertEquals("FamilyName", attribute.getFriendlyName());
        assertEquals("Smith", getAttributeValue(attribute));

        attribute = attributes.get(2);
        assertEquals("http://eidas.europa.eu/attributes/naturalperson/DateOfBirth", attribute.getName());
        assertEquals("DateOfBirth", attribute.getFriendlyName());
        assertEquals("1990-01-01", getAttributeDateValueString(attribute));

        attribute = attributes.get(3);
        assertEquals("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier", attribute.getName());
        assertEquals("PersonIdentifier", attribute.getFriendlyName());
        assertEquals("UK/EU/123456", getAttributeValue(attribute));
    }

    public static void checkAssertionStatementsValid(Response decryptedEidasResponse) {

        Assertion eidasAssertion = decryptedEidasResponse.getAssertions().get(0);

        assertEquals(2, eidasAssertion.getStatements().size());
        assertEquals(1, eidasAssertion.getAttributeStatements().size());
        assertEquals(1, eidasAssertion.getAuthnStatements().size());
        assertEquals(
                EidasConstants.EIDAS_LOA_SUBSTANTIAL,
                eidasAssertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()
        );
    }

    public static void assertResponseForIdentityVerifiedStatus(Response decryptedEidasResponse) {
        assertEquals(StatusCode.SUCCESS, decryptedEidasResponse.getStatus().getStatusCode().getValue());
    }

    public static void assertResponseForCancelledStatus(Response decryptedEidasResponse) {
        assertEquals(StatusCode.RESPONDER, decryptedEidasResponse.getStatus().getStatusCode().getValue());
    }

    public static void assertResponseForAuthenticationFailedStatus(Response decryptedEidasResponse) {
        assertEquals(StatusCode.AUTHN_FAILED, decryptedEidasResponse.getStatus().getStatusCode().getValue());
    }

    private static String getAttributeDateValueString(Attribute attribute) {
        return ((DateOfBirthTypeImpl) attribute.getAttributeValues().get(0)).formatDate();
    }

    private static String getAttributeValue(Attribute attribute) {
        return AttributeUtils.getAttributeStringValue(attribute);
    }
}
