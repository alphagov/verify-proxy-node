package uk.gov.ida.notification.saml.converters;

import com.google.common.base.Strings;
import io.dropwizard.jersey.errors.ErrorMessage;
import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
@Provider
public class ResponseParameterProvider implements ParamConverterProvider {
    private final SamlParser samlParser;

    public ResponseParameterProvider() throws ParserConfigurationException {
        samlParser = new SamlParser();
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (!org.opensaml.saml.saml2.core.Response.class.equals(rawType)) {
            return null;
        }

        return new ParamConverter<T>() {
            @Override
            public T fromString(String value) {
                if (Strings.isNullOrEmpty(value)) {
                    return null;
                }

                String decodedSaml = Base64.decodeAsString(value);
                T authnRequest = samlParser.parseSamlString(decodedSaml);
                return authnRequest;
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
        };
    }
}
