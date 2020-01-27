package uk.gov.ida.eidas.metatron.resources;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.eidas.metatron.domain.EidasConfig;
import uk.gov.ida.eidas.metatron.domain.EidasCountryConfig;
import uk.gov.ida.eidas.metatron.domain.MetadataResolverService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Iterator;

@Path("/")
public class MetatronResource {

    EidasConfig config;
    MetadataResolverService metadataResolverService;

    public MetatronResource(EidasConfig config, MetadataResolverService metadataResolverService) {
        this.config = config;
        this.metadataResolverService = metadataResolverService;
    }

    @GET
    @Path("/config")
    public String config() {
        return config.getCountries().stream()
                .map(EidasCountryConfig::toString)
                .reduce("", (in, config) -> in + config);
    }

    @GET
    @Path("/metadata-truststore/{country}")
    public String truststore(@PathParam("country") String country) throws KeyStoreException {
        KeyStore truststore = config.getCountries().stream()
                .filter(eidasCountryConfig -> eidasCountryConfig.getName().equalsIgnoreCase(country))
                .findFirst().orElseThrow(RuntimeException::new).getMetadataTruststore();

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

    @GET
    @Path("/metadata/{entityId}")
    public String metadata(@PathParam("entityId") String entityId) throws ResolverException {
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(entityId));
        EntityDescriptor entityDescriptor = this.metadataResolverService.getMetadataResolver(entityId).resolveSingle(criteria);
        return entityDescriptor.getEntityID();
    }

}


