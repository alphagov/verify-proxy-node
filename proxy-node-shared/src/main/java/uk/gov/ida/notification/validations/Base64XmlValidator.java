package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.Base64;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Base64XmlValidator implements ConstraintValidator<ValidBase64Xml, String> {

    @Override
    public void initialize(ValidBase64Xml constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String potentialXML, ConstraintValidatorContext context) {
        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (StringUtils.isBlank(potentialXML)) { return true; }

        String xml = Base64.decodeAsString(potentialXML);
        if (StringUtils.isEmpty(xml)) { return false; }

        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilder parser = null;
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = null;
            document = parser.parse(stream);
            return document != null;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            context.buildConstraintViolationWithTemplate("Unexpected exception: " + e.getMessage()).addConstraintViolation();
            return false;
        }
    }
}