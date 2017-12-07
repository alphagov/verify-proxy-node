package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.translation.HubResponse;
import uk.gov.ida.notification.saml.translation.HubResponseTranslator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EidasResponseGeneratorTest {

    @Test
    public void shouldGenerateEidasResponse() throws Throwable {
        HubResponseTranslator hubResponseTranslator = mock(HubResponseTranslator.class);
        EidasResponseGenerator eidasResponseGenerator = new EidasResponseGenerator(hubResponseTranslator);
        HubResponse hubResponse = mock(HubResponse.class);
        Response translatedEidasResponse = mock(Response.class);
        when(hubResponseTranslator.translate(hubResponse)).thenReturn(translatedEidasResponse);

        Response eidasResponse = eidasResponseGenerator.generate(hubResponse);

        assertEquals(translatedEidasResponse, eidasResponse);
    }


}
