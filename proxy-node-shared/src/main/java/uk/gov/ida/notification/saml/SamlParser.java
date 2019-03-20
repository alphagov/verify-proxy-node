package uk.gov.ida.notification.saml;

import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.exceptions.saml.SamlParsingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * Due to security requirements, {@link DocumentBuilder} and
 * {@link DocumentBuilderFactory} should *only* be used via
 * the utility methods in this class.  For more information on the vulnerabilities
 * identified, see the tests.
 */
@SuppressWarnings("unchecked")
public class SamlParser {
    public <T extends XMLObject> T parseSamlString(String xmlString) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());

        try {
            return (T) ObjectUtils.unmarshall(inputStream, XMLObject.class);
        } catch (XMLParserException | UnmarshallingException e) {
            throw new SamlParsingException(e);
        }
    }
}
