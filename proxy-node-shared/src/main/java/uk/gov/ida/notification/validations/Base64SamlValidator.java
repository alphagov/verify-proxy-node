package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.impl.ResponseImpl;
import uk.gov.ida.notification.saml.SamlParser;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Base64SamlValidator implements ConstraintValidator<ValidBase64Saml, String> {

    public static final int MinLength = 64;
    public static final int MaxLength = 64000;

    @Override
    public void initialize(ValidBase64Saml constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || StringUtils.isBlank(value)) { return true; } // @NotNull should detect nulls

        try {
            String decoded = Base64.decodeAsString(value);
            if (StringUtils.isBlank(decoded)) {
                return false;
            }

            Object saml = new SamlParser().parseSamlString(decoded);
            return saml instanceof ResponseImpl;

        } catch (Exception e) {
            return false; // Base64 or SAML was not well formed
        }
    }
}
