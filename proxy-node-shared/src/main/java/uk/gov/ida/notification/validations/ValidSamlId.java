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
@Constraint(validatedBy = { SamlIdValidator.class })
@Size(min = SamlIdValidator.MinLength, max = SamlIdValidator.MaxLength)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidSamlId {

    String message() default "is not a valid XML/SAML id";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
