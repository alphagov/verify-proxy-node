package uk.gov.ida.notification.stubconnector;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;

import java.util.Arrays;
import java.util.List;

public class RequestUtils {
    private final static SecureRandomIdentifierGenerationStrategy idGenerator = new SecureRandomIdentifierGenerationStrategy();

    public static List<String> getMinimumEidasRequestedAttributes() {
        return Arrays.asList(
            AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME,
            AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME,
            AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME,
            AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME
        );
    }

    public static String generateId() {
        return idGenerator.generateIdentifier(true);
    }
}
