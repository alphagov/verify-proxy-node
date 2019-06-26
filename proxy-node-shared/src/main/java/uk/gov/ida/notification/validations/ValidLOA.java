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
@Constraint(validatedBy = { LevelOfAssuranceValidator.class })
@Size(min = LevelOfAssuranceValidator.MinLength, max = LevelOfAssuranceValidator.MaxLength)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidLOA {

    String message() default "is not an accepted Level Of Assurance";

    public boolean acceptVerifyLOA() default true;
    public boolean acceptEidasLOA() default true;

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
