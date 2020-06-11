package uk.gov.ida.notification.translator.saml;

import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario;

import java.net.URI;
import java.util.Optional;

public class HubResponseContainer {

    private final String pid;
    private final String eidasRequestId;
    private final URI destinationURL;
    private final URI issuer;
    private final Attributes attributes;
    private final VspScenario vspScenario;
    private final VspLevelOfAssurance levelOfAssurance;
    private final boolean transientPidRequested;


    public HubResponseContainer(
            final HubResponseTranslatorRequest hubResponseTranslatorRequest,
            final TranslatedHubResponse translatedHubResponse) {
        this.pid = translatedHubResponse.getPid().orElse(null);
        this.eidasRequestId = hubResponseTranslatorRequest.getEidasRequestId();
        this.destinationURL = hubResponseTranslatorRequest.getDestinationUrl();
        this.issuer = hubResponseTranslatorRequest.getEidasIssuerEntityId();
        this.transientPidRequested = hubResponseTranslatorRequest.isTransientPidRequested();
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

    URI getIssuer() {
        return issuer;
    }

    public boolean isTransientPidRequested() {
        return transientPidRequested;
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
