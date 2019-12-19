package uk.gov.ida.eidas.metatron.resources;

import uk.gov.ida.eidas.metatron.core.dto.EidasConfig;
import uk.gov.ida.eidas.metatron.core.dto.EidasCountryConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Iterator;

@Path("/")
public class MetatronResource {

    EidasConfig config;

    public MetatronResource(EidasConfig config) {
        this.config = config;
    }

    @GET
    @Path("/config")
    public String config() {
        return config.getCountries().stream()
                .map(EidasCountryConfig::toString)
                .reduce("", (in, config) -> in + config) ;
    }

    @GET
    @Path("/truststore")
    public String truststore() throws KeyStoreException {
        KeyStore truststore = config.getKeyStore();
        Iterator<String> iterator = truststore.aliases().asIterator();

        StringBuffer trustedCerts = new StringBuffer();

        iterator.forEachRemaining(alias -> {
            try {
                trustedCerts.append("<h1>" + alias + "</h1><pre>" + truststore.getCertificate(alias).toString() + "</pre><hr/>");
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        });
        return trustedCerts.toString();
    }
}


