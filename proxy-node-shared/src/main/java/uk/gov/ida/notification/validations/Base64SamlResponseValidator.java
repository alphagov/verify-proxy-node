package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.impl.ResponseImpl;
import uk.gov.ida.notification.saml.SamlParser;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;

public class Base64SamlResponseValidator implements ConstraintValidator<ValidBase64SamlResponse, String> {

    public static final int MinLength = 64;
    public static final int MaxLength = 64000;

    @Override
    public void initialize(ValidBase64SamlResponse constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String potentialSAML, ConstraintValidatorContext context) {
        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (StringUtils.isBlank(potentialSAML)) {
            return true;
        }

        String decoded = Base64.decodeAsString(potentialSAML);
        if (StringUtils.isBlank(decoded)) {
            return false;
        }

        ResponseImpl saml = new SamlParser().parseSamlString(decoded);
        return saml != null;
    }
}