package uk.gov.ida.notification.contracts.verifyserviceprovider;

public class TranslatedHubResponseBuilder {

    private String pid = "123456";
    private VspLevelOfAssurance loa = VspLevelOfAssurance.LEVEL_2;
    private VspScenario vspScenario = VspScenario.IDENTITY_VERIFIED;
    private Attributes attributes = new AttributesBuilder().build();

    public static TranslatedHubResponse buildTranslatedHubResponseIdentityVerified() {
        return new TranslatedHubResponseBuilder().build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseAuthenticationFailed() {
        return new TranslatedHubResponseBuilder().withScenario(VspScenario.AUTHENTICATION_FAILED).withoutAttributes().build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseRequestError() {
        return new TranslatedHubResponseBuilder().withScenario(VspScenario.REQUEST_ERROR).withoutAttributes().build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseCancellation() {
        return new TranslatedHubResponseBuilder().withScenario(VspScenario.CANCELLATION).withoutAttributes().build();
    }

    public TranslatedHubResponse build() {
        return new TranslatedHubResponse(vspScenario, pid, loa, attributes);
    }

    public TranslatedHubResponseBuilder withScenario(VspScenario scenario) {
        this.vspScenario = scenario;
        return this;
    }

    public TranslatedHubResponseBuilder withAttributes(Attributes attributes) {
        this.attributes = attributes;
        return this;
    }

    public TranslatedHubResponseBuilder withoutAttributes() {
        this.attributes = null;
        return this;
    }

    public TranslatedHubResponseBuilder withLevelOfAssurance(VspLevelOfAssurance loa) {
        this.loa = loa;
        return this;
    }
}
