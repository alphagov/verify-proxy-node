package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.impl.AuthnRequestImpl;
import uk.gov.ida.notification.exceptions.saml.SamlParsingException;
import uk.gov.ida.notification.saml.SamlParser;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Base64SamlAuthnRequestValidator implements ConstraintValidator<ValidBase64SamlAuthnRequest, String> {

    public static final int MinLength = 64;
    public static final int MaxLength = 64000;

    @Override
    public void initialize(ValidBase64SamlAuthnRequest constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String potentialSAML, ConstraintValidatorContext context) {
        // return false; //HA

        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (StringUtils.isBlank(potentialSAML)) {
            return true;
        }

        String decoded;
        try {
            decoded = Base64.decodeAsString(potentialSAML);
            if (StringUtils.isBlank(decoded)) {
                return false;
            }

        } catch (Exception e) {
            // Base64 decoding can throw runtime exceptions
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Could not decode input as Base64: " + e.getMessage()).addConstraintViolation();
            return false;
        }

        try {
            AuthnRequestImpl saml = new SamlParser().parseSamlString(decoded);
            return saml != null; // successfully constructed a SAML object

        } catch (SamlParsingException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Could not find valid SAML Authn Request after Base64 decoding.").addConstraintViolation();
            return false;
        }
    }
}
