package uk.gov.ida.notification.translator.saml;

import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario;

import java.net.URI;

public class HubResponseContainer {

    private String pid;
    private String eidasRequestId;
    private URI destinationURL;
    private Attributes attributes;
    private VspScenario vspScenario;
    private VspLevelOfAssurance levelOfAssurance;

    public HubResponseContainer(HubResponseTranslatorRequest hubResponseTranslatorRequest, TranslatedHubResponse translatedHubResponse) {
        this.pid = translatedHubResponse.getPid();
        this.eidasRequestId = hubResponseTranslatorRequest.getEidasRequestId();
        this.destinationURL = hubResponseTranslatorRequest.getDestinationUrl();
        this.attributes = translatedHubResponse.getAttributes();
        this.vspScenario = translatedHubResponse.getScenario();
        this.levelOfAssurance = translatedHubResponse.getLevelOfAssurance();
    }

    String getPid() {
        return pid;
    }

    String getEidasRequestId() {
        return eidasRequestId;
    }

    String getDestinationURL() {
        return destinationURL.toString();
    }

    Attributes getAttributes() {
        return attributes;
    }

    VspScenario getVspScenario() {
        return vspScenario;
    }

    VspLevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }
}
