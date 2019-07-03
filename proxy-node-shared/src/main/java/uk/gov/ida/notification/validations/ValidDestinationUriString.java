package uk.gov.ida.notification.validations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = { DestinationUriStringValidator.class })
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidDestinationUriString {

    String message() default "is not a valid destination uri";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
