package uk.gov.ida.notification;

import uk.gov.ida.notification.eidassaml.RequestDto;
import uk.gov.ida.notification.eidassaml.ResponseDto;

public class EidasSamlParserService {

    public ResponseDto validate(RequestDto eidasSamlParserRequest) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.issuer = "issuer";
        responseDto.requestId = "request id";
        return responseDto;
    }
}
