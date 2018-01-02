package uk.gov.ida.notification.saml.converters;

import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
@Provider
public class AuthnRequestParameterProvider implements ParamConverterProvider {
    private final SamlParser samlParser;

    public AuthnRequestParameterProvider() throws ParserConfigurationException {
        samlParser = new SamlParser();
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (!AuthnRequest.class.equals(rawType)) {
            return null;
        }
        return new EidasSamlParamConverter<>(samlParser);
    }
}
