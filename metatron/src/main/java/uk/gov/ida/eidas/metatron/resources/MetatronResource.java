package uk.gov.ida.eidas.metatron.resources;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.eidas.metatron.core.dto.EidasConfig;
import uk.gov.ida.eidas.metatron.core.dto.EidasCountryConfig;
import uk.gov.ida.eidas.metatron.core.dto.MetatronMetadataResolverRepository;
import uk.gov.ida.saml.metadata.JerseyClientMetadataResolver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Path("/")
public class MetatronResource {

    private EidasConfig config;
    private MetatronMetadataResolverRepository metatronMetadataResolverRepository;

    public MetatronResource(EidasConfig config, Client client) {
        this.config = config;
        this.metatronMetadataResolverRepository = new MetatronMetadataResolverRepository(config, client);
    }

    @GET
    @Path("/config")
    public String config() {
        return config.getCountries().stream()
                .map(EidasCountryConfig::toString)
                .reduce("", (in, config) -> in + config) ;
    }

    @GET
    @Path("/all-metadata")
    public String allMetadata() throws Exception {
        Collection<EidasCountryConfig> countries = config.getCountries();
        List<String> things = new ArrayList<>();
        for (EidasCountryConfig country : countries) {
            String entityId = country.getEntityId();
            JerseyClientMetadataResolver metadataResolver = (JerseyClientMetadataResolver) metatronMetadataResolverRepository.getMetadataResolver(entityId).orElseThrow();

            EntityDescriptor entityDescriptor = metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId)));
            if (entityDescriptor != null) {
                things.add(entityDescriptor.getEntityID());
            }
        }
        var returnString = String.join(", ", things);
        return returnString.isEmpty() ? "Nothing here buddy" : returnString;
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


