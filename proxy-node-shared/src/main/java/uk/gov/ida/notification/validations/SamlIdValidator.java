package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SamlIdValidator implements ConstraintValidator<ValidSamlId, String> {

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
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) { return true; } // @NotNull should detect nulls
        if (Character.isDigit(value.charAt(0))) { return false; }
        if (value.length() < MinLength || value.length() > MaxLength) { return false; }
        return !value.contains(" ") && !value.contains("\n");
    }
}
