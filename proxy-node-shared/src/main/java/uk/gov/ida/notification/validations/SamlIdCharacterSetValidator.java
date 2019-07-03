package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SamlIdCharacterSetValidator implements ConstraintValidator<ValidSamlId, String> {

    public static final int MinLength = 20; // 160 bits
    public static final int MaxLength = 256; // a reasonable limit, UUIDs are 40 characters

    @Override
    public void initialize(ValidSamlId constraint) { /* intentionally blank */ }

    /**
     * The exact method of generating SAML IDs is not explicitly defined. It just needs to conform the standards of an
     * XML ID. An XML ID is an xsd:NCName, which is derived from xsd:Name, which can't start with a number or contain
     * spaces, and should have 160 bits of "randomness". This validation also rejects new lines.
     */
    @Override
    public boolean isValid(String potentialId, ConstraintValidatorContext context) {
        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (StringUtils.isBlank(potentialId)) { return true; }

        boolean pass = true;
        if (Character.isDigit(potentialId.charAt(0))) {
            context.buildConstraintViolationWithTemplate("An id cannot start with a digit.").addConstraintViolation();
            pass = false;
        }
        if (potentialId.contains(" ") || potentialId.contains("\n")) {
            context.buildConstraintViolationWithTemplate("An id should not contain whitespace.").addConstraintViolation();
            pass = false;
        }

        if (!pass) {
            context.disableDefaultConstraintViolation();
        }

        return pass;
    }
}
