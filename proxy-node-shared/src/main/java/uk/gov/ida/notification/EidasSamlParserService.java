package uk.gov.ida.notification;

import uk.gov.ida.notification.dto.EidasSamlParserRequest;
import uk.gov.ida.notification.dto.EidasSamlParserResponse;

public class EidasSamlParserService {

    public EidasSamlParserResponse parse(EidasSamlParserRequest eidasSamlParserRequest) {
        return new EidasSamlParserResponse("request id", "issuer", "pub enc key", "destination");
    }
}
