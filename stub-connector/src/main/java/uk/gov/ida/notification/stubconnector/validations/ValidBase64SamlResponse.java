package uk.gov.ida.notification.stubconnector.validations;

import uk.gov.ida.notification.validations.Base64XmlValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = { Base64SamlResponseValidator.class })
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidBase64SamlResponse {

    String message() default "is not Base64 encoded SAML";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
