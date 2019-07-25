package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.Base64;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Base64XmlValidator implements ConstraintValidator<ValidBase64Xml, String> {

    @Override
    public void initialize(ValidBase64Xml constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String potentialBase64, ConstraintValidatorContext context) {
        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (StringUtils.isBlank(potentialBase64)) { return true; }

        try {
            String decoded = Base64.decodeAsString(potentialBase64);
            if (StringUtils.isBlank(decoded)) { return false; }
            return decoded.trim().startsWith("<?xml version=");

        } catch (RuntimeException e) {
            context.buildConstraintViolationWithTemplate("Exception: " + e.getMessage()).addConstraintViolation();
            return false; // could not create a URI from the String
        }
    }
}
