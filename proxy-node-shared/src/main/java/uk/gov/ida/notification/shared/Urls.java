package uk.gov.ida.notification.shared;

public interface Urls {

    interface EidasSamlParserUrls {
        String EIDAS_AUTHN_REQUEST_PATH = "/eidasAuthnRequest";
    }

    interface GatewayUrls {
        String GATEWAY_ROOT = "/SAML2/SSO";
        String GATEWAY_EIDAS_AUTHN_REQUEST_POST_PATH = "/POST";
        String GATEWAY_EIDAS_AUTHN_REQUEST_REDIRECT_PATH = "/Redirect";
        String GATEWAY_HUB_RESPONSE_PATH = "/Response/POST";
        String GATEWAY_HUB_RESPONSE_RESOURCE = GATEWAY_ROOT + GATEWAY_HUB_RESPONSE_PATH;
    }

    interface TranslatorUrls {
        String TRANSLATOR_ROOT = "/translator";
        String TRANSLATE_HUB_RESPONSE_PATH = "/translate-hub-response";
        String TRANSLATOR_HUB_RESPONSE_RESOURCE = TRANSLATOR_ROOT + TRANSLATE_HUB_RESPONSE_PATH;
    }

    interface VerifyServiceProviderUrls {
        String TRANSLATE_HUB_RESPONSE_ENDPOINT = "/translate-response";
        String GENERATE_HUB_AUTHN_REQUEST_ENDPOINT = "/generate-request";
    }
}
