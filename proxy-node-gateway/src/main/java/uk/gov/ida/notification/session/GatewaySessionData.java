package uk.gov.ida.notification.session;


import org.hibernate.validator.constraints.NotEmpty;

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
        String hubRequestId,
        String eidasRequestId,
        String eidasDestination,
        String eidasRelayState,
        String eidasConnectorPublicKey
    ) {
        this.hubRequestId = hubRequestId;
        this.eidasRequestId = eidasRequestId;
        this.eidasDestination = eidasDestination;
        this.eidasRelayState = eidasRelayState;
        this.eidasConnectorPublicKey = eidasConnectorPublicKey;
    }

    public String getHubRequestId() { return this.hubRequestId; }

    public String getEidasRequestId() { return this.eidasRequestId; }

    public String getEidasDestination() { return this.eidasDestination; }

    public String getEidasRelayState() { return this.eidasRelayState; }

    public String getEidasConnectorPublicKey() { return this.eidasConnectorPublicKey; }
}
