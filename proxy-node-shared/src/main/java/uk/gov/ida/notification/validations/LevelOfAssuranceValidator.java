package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LevelOfAssuranceValidator implements ConstraintValidator<ValidLOA, String> {

    public static final int MinLength = 7;
    public static final int MaxLength = 28;

    private boolean acceptVerifyLOA;
    private boolean acceptEidasLOA;

    @Override
    public void initialize(ValidLOA constraint) {
        this.acceptVerifyLOA = constraint.acceptVerifyLOA();
        this.acceptEidasLOA = constraint.acceptEidasLOA();
    }

    @Override
    public boolean isValid(String potentialLOA, ConstraintValidatorContext context) {
        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (StringUtils.isBlank(potentialLOA)) { return true; }

        return
            (acceptVerifyLOA && EnumUtils.isValidEnum(VspLevelOfAssurance.class, potentialLOA)) ||
            (acceptEidasLOA && EnumUtils.isValidEnum(EidasLoaEnum.class, potentialLOA));
    }
}