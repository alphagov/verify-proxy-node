package uk.gov.ida.notification.translator.saml;

import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario;

import java.net.URI;
import java.util.Optional;

public class HubResponseContainer {

    private final Optional<String> pid;
    private final String eidasRequestId;
    private final URI destinationURL;
    private final URI issuer;
    private final Attributes attributes;
    private final VspScenario vspScenario;
    private final VspLevelOfAssurance levelOfAssurance;

    public HubResponseContainer(
            final HubResponseTranslatorRequest hubResponseTranslatorRequest,
            final TranslatedHubResponse translatedHubResponse,
            final IdentifierGenerationStrategy identifierGenerator) {
        this.pid = generatePidForNameID(hubResponseTranslatorRequest, translatedHubResponse, identifierGenerator);
        this.eidasRequestId = hubResponseTranslatorRequest.getEidasRequestId();
        this.destinationURL = hubResponseTranslatorRequest.getDestinationUrl();
        this.issuer = hubResponseTranslatorRequest.getEidasIssuerEntityId();
        this.attributes = translatedHubResponse.getAttributes().orElse(null);
        this.vspScenario = translatedHubResponse.getScenario();
        this.levelOfAssurance = translatedHubResponse.getLevelOfAssurance().orElse(null);
    }

    private Optional<String> generatePidForNameID(
            HubResponseTranslatorRequest hubResponseTranslatorRequest,
            TranslatedHubResponse translatedHubResponse,
            IdentifierGenerationStrategy identifierGenerator) {
        if (hubResponseTranslatorRequest.isTransientId()) {
            return Optional.of(identifierGenerator.generateIdentifier());
        }
        return translatedHubResponse.getPid();
    }

    Optional<String> getPid() {
        return pid;
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
