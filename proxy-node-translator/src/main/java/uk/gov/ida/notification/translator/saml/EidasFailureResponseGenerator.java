package uk.gov.ida.notification.translator.saml;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.notification.saml.EidasResponseBuilder;

import javax.ws.rs.core.Response.Status;
import java.util.function.Supplier;

public class EidasFailureResponseGenerator {

    private Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier;

    public EidasFailureResponseGenerator(
            Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier
    ) {
        this.eidasResponseBuilderSupplier = eidasResponseBuilderSupplier;
    }

    Response generateFailureSamlResponse(
            String issuer,
            Status responseStatus,
            String eidasRequestId,
            String destinationUrl,
            String entityID) {
        return eidasResponseBuilderSupplier.get()
                .withIssuer(issuer)
                .withStatus(getMappedStatusCode(responseStatus))
                .withInResponseTo(eidasRequestId)
                .withIssueInstant(DateTime.now())
                .withDestination(destinationUrl)
                .withAssertionConditions(entityID)
                .build();
    }

    private static String getMappedStatusCode(Status responseStatus) {
        switch (responseStatus) {
            case BAD_REQUEST:
                return StatusCode.REQUESTER;
            case INTERNAL_SERVER_ERROR:
                return StatusCode.RESPONDER;
            default:
                return StatusCode.RESPONDER;
        }
    }
}
