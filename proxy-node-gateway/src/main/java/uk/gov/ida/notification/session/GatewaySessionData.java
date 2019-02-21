package uk.gov.ida.notification.session;


import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;

public class GatewaySessionData {
    @NotEmpty
    private final String hubRequestId;

    @NotEmpty
    private final String eidasRequestId;

    @NotEmpty
    private final String eidasDestination;

    private final String eidasRelayState;

    @NotEmpty
    private final String eidasConnectorPublicKey;

    public GatewaySessionData(
        EidasSamlParserResponse eidasSamlParserResponse,
        AuthnRequestResponse vspResponse,
        String eidasRelayState
    ) {
        this.hubRequestId = vspResponse.getRequestId();
        this.eidasRequestId = eidasSamlParserResponse.getRequestId();
        this.eidasDestination = eidasSamlParserResponse.getDestination();
        this.eidasRelayState = eidasRelayState;
        this.eidasConnectorPublicKey = eidasSamlParserResponse.getConnectorEncryptionPublicCertificate();
    }

    public String getHubRequestId() { return this.hubRequestId; }

    public String getEidasRequestId() { return this.eidasRequestId; }

    public String getEidasDestination() { return this.eidasDestination; }

    public String getEidasRelayState() { return this.eidasRelayState; }

    public String getEidasConnectorPublicKey() { return this.eidasConnectorPublicKey; }
}
