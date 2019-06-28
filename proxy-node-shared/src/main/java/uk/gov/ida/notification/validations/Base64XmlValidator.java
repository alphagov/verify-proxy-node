package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.Base64;
import org.w3c.dom.Document;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Base64XmlValidator implements ConstraintValidator<ValidBase64Xml, String> {

    @Override
    public void initialize(ValidBase64Xml constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) { return true; } // @NotNull should detect nulls

        String xml = Base64.decodeAsString(value);
        if (StringUtils.isEmpty(xml)) { return false; }

        try {
            InputStream stream = new ByteArrayInputStream(xml.getBytes());
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(stream);
            return document != null;

        } catch (Exception e) {
            return false;
        }
    }
}
