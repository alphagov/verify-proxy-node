package uk.gov.ida.notification.session;

import uk.gov.ida.notification.exceptions.SessionAttributeException;

import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.ida.notification.session.SessionKeys.SESSION_KEY_SESSION_DATA;

public class GatewaySessionDataValidator {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    public static final String NOT_NULL_MESSAGE = "Session data can not be null";

    public static GatewaySessionData getValidatedSessionData(HttpSession session) {
        GatewaySessionData sessionData = (GatewaySessionData) session.getAttribute(SESSION_KEY_SESSION_DATA);
        if (sessionData == null) throw new SessionAttributeException(NOT_NULL_MESSAGE, session.getId());

        Set<ConstraintViolation<GatewaySessionData>> violations = validator.validate(sessionData);
        if (violations.size() > 0) {
            List<String> collect = violations
                .stream()
                .map(GatewaySessionDataValidator::combinePropertyAndMessage)
                .sorted()
                .collect(Collectors.toList());

            throw new SessionAttributeException(String.join(", ", collect), session.getId());
        }

        return sessionData;
    }

    private static String combinePropertyAndMessage(ConstraintViolation violation) {
        return String.format("%s field %s", violation.getPropertyPath().toString(), violation.getMessage());
    }
}
