package uk.gov.ida.notification.translator.saml;

import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario;

import java.net.URI;
import java.util.Optional;

public class HubResponseContainer {

    private String pid;
    private String eidasRequestId;
    private URI destinationURL;
    private URI issuer;
    private Attributes attributes;
    private VspScenario vspScenario;
    private VspLevelOfAssurance levelOfAssurance;

    public HubResponseContainer(HubResponseTranslatorRequest hubResponseTranslatorRequest, TranslatedHubResponse translatedHubResponse) {
        this.pid = translatedHubResponse.getPid().orElse(null);
        this.eidasRequestId = hubResponseTranslatorRequest.getEidasRequestId();
        this.destinationURL = hubResponseTranslatorRequest.getDestinationUrl();
        this.issuer = hubResponseTranslatorRequest.getEidasIssuer();
        this.attributes = translatedHubResponse.getAttributes().orElse(null);
        this.vspScenario = translatedHubResponse.getScenario();
        this.levelOfAssurance = translatedHubResponse.getLevelOfAssurance().orElse(null);
    }

    Optional<String> getPid() {
        return Optional.ofNullable(pid);
    }

    String getEidasRequestId() {
        return eidasRequestId;
    }

    String getDestinationURL() {
        return destinationURL.toString();
    }

    public URI getIssuer() {
        return issuer;
    }

    Optional<Attributes> getAttributes() {
        return Optional.ofNullable(attributes);
    }

    VspScenario getVspScenario() {
        return vspScenario;
    }

    Optional<VspLevelOfAssurance> getLevelOfAssurance() {
        return Optional.ofNullable(levelOfAssurance);
    }
}
