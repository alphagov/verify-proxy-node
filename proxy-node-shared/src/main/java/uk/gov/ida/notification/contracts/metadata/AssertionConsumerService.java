package uk.gov.ida.notification.contracts.metadata;

import uk.gov.ida.notification.validations.ValidDestinationUri;

import javax.validation.constraints.NotNull;
import java.net.URI;

public class AssertionConsumerService {

    @NotNull
    @ValidDestinationUri
    private URI location;

    private int index;

    private boolean defaultService;

    // Needed for serialisation
    public AssertionConsumerService() {
    }

    public AssertionConsumerService(
            URI location,
            int index,
            boolean defaultService) {
        this.location = location;
        this.index = index;
        this.defaultService = defaultService;
    }

    public URI getLocation() {
        return this.location;
    }

    public int getIndex() {
        return index;
    }

    public boolean isDefaultService() {
        return defaultService;
    }
}
