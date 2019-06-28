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

    // self-signed test cert is ~900-1000 chars
    public static final int MinLength = 512;
    public static final int MaxLength = 2048;

    @Override
    public void initialize(ValidPEM constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) { return true; } // @NotNull should detect nulls

        try {
            X509CertificateFactory factory = new X509CertificateFactory();
            X509Certificate cert = factory.createCertificate(value);
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
            return false;
        }
    }
}
