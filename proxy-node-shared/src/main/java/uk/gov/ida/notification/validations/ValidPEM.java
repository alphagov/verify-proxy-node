package uk.gov.ida.notification.validations;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = { PEMCertificateValidator.class })
@Size(min = PEMCertificateValidator.MinLength, max = PEMCertificateValidator.MaxLength)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidPEM {

    String message() default "is not a valid PEM certificate, or has expired";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
