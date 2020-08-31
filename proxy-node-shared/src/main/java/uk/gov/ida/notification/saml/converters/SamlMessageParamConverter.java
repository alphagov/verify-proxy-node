package uk.gov.ida.notification.saml.converters;

import com.google.common.base.Strings;
import io.dropwizard.jersey.errors.ErrorMessage;
import uk.gov.ida.Base64;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;

class SamlMessageParamConverter<T> implements ParamConverter<T> {
    private SamlParser samlParser;

    SamlMessageParamConverter(SamlParser samlParser) {
        this.samlParser = samlParser;
    }

    @Override
    public T fromString(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        String decodedSaml = Base64.decodeToString(value);
        T parsedSaml = samlParser.parseSamlString(decodedSaml);
        return parsedSaml;
    }

    @Override
    public String toString(T value) {
        return value.toString();
    }

    protected Response getErrorResponse(String message) {
        return Response
                .status(400)
                .entity(new ErrorMessage(400, message))
                .build();
    }
}
