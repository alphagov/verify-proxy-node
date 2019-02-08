package uk.gov.ida.notification.services;

import uk.gov.ida.notification.dto.EidasSamlParserRequest;
import uk.gov.ida.notification.dto.EidasSamlParserResponse;
import uk.gov.ida.notification.exceptions.EidasSamlParserResponseException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Set;

public class EidasSamlParserService {
    private final Client eidasSamlParserClient;
    private final String eidasSamlParserUrl;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public EidasSamlParserService(
            Client eidasSamlParserClient,
            String eidasSamlParserUrl) {
        this.eidasSamlParserClient = eidasSamlParserClient;
        this.eidasSamlParserUrl = eidasSamlParserUrl;
    }

    public EidasSamlParserResponse parse(EidasSamlParserRequest eidasSamlParserRequest) {
        Response response = eidasSamlParserClient.target(eidasSamlParserUrl)
                .request()
                .post(Entity.json(eidasSamlParserRequest));

        return validateEidasSamlResponseParserDTO(extractEidasSamlParserResponse(response));

    }

    private EidasSamlParserResponse extractEidasSamlParserResponse(Response response) {
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new EidasSamlParserResponseException(
                String.format(
                    "Received a '%s' status code response: %s",
                    response.getStatus(),
                    response.getStatusInfo().getReasonPhrase()
                )
            );
        } else {
            try {
                return response.readEntity(EidasSamlParserResponse.class);
            } catch (ProcessingException e) {
                throw new EidasSamlParserResponseException(e);
            }
        }
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
