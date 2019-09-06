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
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME;

public class TranslatedHubResponseTestAssertions {

    public static void checkAllAttributesValid(Response decryptedEidasResponse) {
        Assertion eidasAssertion = decryptedEidasResponse.getAssertions().get(0);
        List<Attribute> attributes = eidasAssertion.getAttributeStatements().get(0).getAttributes();

        assertThat(attributes).hasSize(4);

        final Attribute firstNameAttribute = attributes.get(0);
        assertThat(firstNameAttribute.getName()).isEqualTo(EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME);
        assertThat(firstNameAttribute.getFriendlyName()).isEqualTo(EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME);
        assertThat(getAttributeValue(firstNameAttribute)).isEqualTo("Jean Paul");

        final Attribute familyNameAttribute = attributes.get(1);
        assertThat(familyNameAttribute.getName()).isEqualTo(EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME);
        assertThat(familyNameAttribute.getFriendlyName()).isEqualTo(EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME);
        assertThat(getAttributeValue(familyNameAttribute)).isEqualTo("Smith");

        final Attribute DobAttribute = attributes.get(2);
        assertThat(DobAttribute.getName()).isEqualTo(EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME);
        assertThat(DobAttribute.getFriendlyName()).isEqualTo(EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME);
        assertThat(getAttributeDateValueString(DobAttribute)).isEqualTo("1990-01-01");

        final Attribute PidAttribute = attributes.get(3);
        assertThat(PidAttribute.getName()).isEqualTo(EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME);
        assertThat(PidAttribute.getFriendlyName()).isEqualTo(EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME);
        assertThat(getAttributeValue(PidAttribute)).isEqualTo("GB/NATIONALITY_CODE/123456");
    }

    public static void checkAssertionStatementsValid(Response decryptedEidasResponse) {
        Assertion eidasAssertion = decryptedEidasResponse.getAssertions().get(0);

        assertThat(eidasAssertion.getStatements()).hasSize(2);
        assertThat(eidasAssertion.getAttributeStatements()).hasSize(1);
        assertThat(eidasAssertion.getAuthnStatements()).hasSize(1);

        assertThat(eidasAssertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef())
                .isEqualTo(EidasConstants.EIDAS_LOA_SUBSTANTIAL);
    }

    public static void checkResponseStatusCodeValidForIdentityVerifiedStatus(Response decryptedEidasResponse) {
        assertThat(decryptedEidasResponse.getStatus().getStatusCode().getValue()).isEqualTo(StatusCode.SUCCESS);
    }

    public static void checkResponseStatusCodeValidForCancelledStatus(Response decryptedEidasResponse) {
        assertThat(decryptedEidasResponse.getStatus().getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
    }

    public static void checkResponseStatusCodeValidForAuthenticationFailedStatus(Response decryptedEidasResponse) {
        assertThat(decryptedEidasResponse.getStatus().getStatusCode().getValue()).isEqualTo(StatusCode.AUTHN_FAILED);
    }

    private static String getAttributeDateValueString(Attribute attribute) {
        return ((DateOfBirthTypeImpl) attribute.getAttributeValues().get(0)).formatDate();
    }

    private static String getAttributeValue(Attribute attribute) {
        return AttributeUtils.getAttributeStringValue(attribute);
    }
}
