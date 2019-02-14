package uk.gov.ida.notification.translator.saml;

import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;

import java.net.URI;

public class HubResponseContainer {
    private String eidasRequestId;
    private String levelOfAssurance;
    private URI destinationURL;
    private String pid;
    private Attributes attributes;

    public HubResponseContainer(HubResponseTranslatorRequest hubResponseTranslatorRequest, TranslatedHubResponse translatedHubResponse) {
        this.eidasRequestId = hubResponseTranslatorRequest.getEidasRequestId();
        this.levelOfAssurance = hubResponseTranslatorRequest.getLevelOfAssurance();
        this.destinationURL = hubResponseTranslatorRequest.getDestinationUrl();
        this.pid = translatedHubResponse.getPid();
        this.attributes = translatedHubResponse.getAttributes();
    }

    public String getEidasRequestId() {
        return eidasRequestId;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public URI getDestinationURL() {
        return destinationURL;
    }

    public String getPid() {
        return pid;
    }

    public Attributes getAttributes() {
        return attributes;
    }
}
