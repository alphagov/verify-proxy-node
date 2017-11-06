package uk.gov.ida.notification.resources;

import uk.gov.ida.notification.EidasProxyNodeConfiguration;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

@Path("/verify-uk")
public class VerifyResource {

    private final Client client;
    private final EidasProxyNodeConfiguration config;

    public VerifyResource(Client client, EidasProxyNodeConfiguration config) {
        this.client = client;
        this.config = config;
    }

    @POST
    public String handlePost(String req) {
        return client.target(config.getHubUrl())
                .request()
                .post(Entity.text(req))
                .readEntity(String.class);
    }

}
