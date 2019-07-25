package uk.gov.ida.notification.validations;

import org.apache.commons.lang.StringUtils;
import uk.gov.ida.common.shared.security.X509CertificateFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class PEMCertificateValidator implements ConstraintValidator<ValidPEM, String> {

    // Self-signed test cert is ~900-1000 chars.
    // NL cert comes in at just over 2070 chars.
    public static final int MinLength = 512;
    public static final int MaxLength = 4096;

    @Override
    public void initialize(ValidPEM constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String potentialPEM, ConstraintValidatorContext context) {
        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (StringUtils.isBlank(potentialPEM)) { return true; }

        try {
            X509CertificateFactory factory = new X509CertificateFactory();
            X509Certificate cert = factory.createCertificate(potentialPEM);
            cert.checkValidity(new Date()); // throws if invalid right now
            return true;

        } catch (CertificateNotYetValidException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("This certificate is not yet valid.").addConstraintViolation();
            return false;

        } catch (CertificateExpiredException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("This certificate has expired.").addConstraintViolation();
            return false;

        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate("Unexpected exception: " + e.getMessage()).addConstraintViolation();
            return false;
        }
    }
}
