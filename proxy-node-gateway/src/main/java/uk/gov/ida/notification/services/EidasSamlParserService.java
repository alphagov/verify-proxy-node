package uk.gov.ida.notification.services;

import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.notification.dto.EidasSamlParserRequest;
import uk.gov.ida.notification.dto.EidasSamlParserResponse;
import uk.gov.ida.notification.exceptions.EidasSamlParserResponseException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.net.URI;
import java.util.Set;

public class EidasSamlParserService {
    private final JsonClient eidasSamlParserClient;
    private final URI eidasSamlParserURI;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public EidasSamlParserService(
            JsonClient eidasSamlParserClient,
            URI eidasSamlParserURI) {
        this.eidasSamlParserClient = eidasSamlParserClient;
        this.eidasSamlParserURI = eidasSamlParserURI;
    }

    public EidasSamlParserResponse parse(EidasSamlParserRequest eidasSamlParserRequest) {
        EidasSamlParserResponse response = eidasSamlParserClient.post(eidasSamlParserRequest, eidasSamlParserURI, EidasSamlParserResponse.class);

        return validateEidasSamlResponseParserDTO(response);
    }

    private EidasSamlParserResponse validateEidasSamlResponseParserDTO(EidasSamlParserResponse eidasSamlParserResponse) {
        Set<ConstraintViolation<EidasSamlParserResponse>> violations = validator.validate(eidasSamlParserResponse);
        if(violations.isEmpty()) {
            return eidasSamlParserResponse;
        } else {
            throw new EidasSamlParserResponseException(violations);
        }
    }
}
