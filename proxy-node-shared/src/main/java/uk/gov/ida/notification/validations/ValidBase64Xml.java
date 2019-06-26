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
@Constraint(validatedBy = { Base64XmlValidator.class })
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidBase64Xml {

    String message() default "does not seem to be valid XML encoded in Base64";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
