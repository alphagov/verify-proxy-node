package uk.gov.ida.notification.eidassaml.saml.validation.components;

import com.google.common.base.Strings;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.contracts.metadata.AssertionConsumerService;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.shared.proxy.MetatronProxy;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class AssertionConsumerServiceValidator {

    private MetatronProxy metatronProxy;

    public AssertionConsumerServiceValidator(MetatronProxy metatronProxy) {
        this.metatronProxy = metatronProxy;
    }

    public void validate(AuthnRequest request) {
        String assertionConsumerServiceURL = request.getAssertionConsumerServiceURL();
        if (Strings.isNullOrEmpty(assertionConsumerServiceURL)) {
            return;
        }

        final List<String> assertionConsumerServicesLocationsFromMetadata =
                metatronProxy.getCountryMetadata(request.getIssuer().getValue())
                        .getAssertionConsumerServices()
                        .stream()
                        .map(AssertionConsumerService::getLocation)
                        .map(URI::toString)
                        .collect(Collectors.toList());

        if (assertionConsumerServicesLocationsFromMetadata.stream().anyMatch(location -> location.equals(assertionConsumerServiceURL))) {
            return;
        }

        throw new InvalidAuthnRequestException("Supplied AssertionConsumerServiceURL has no match in metadata. " +
                String.format("Supplied: %s. In metadata: %s.", assertionConsumerServiceURL, String.join(", ", assertionConsumerServicesLocationsFromMetadata)));
    }
}
