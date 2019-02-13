package uk.gov.ida.notification.shared;

public interface Urls {

    interface EidasSamlParserUrls {
        String EIDAS_AUTHN_REQUEST_PATH = "/eidasAuthnRequest";
    }

    interface GatewayUrls {
        String GATEWAY_EIDAS_AUTHN_REQUEST_PATH = "/SAML2/SSO";
        String GATEWAY_HUB_RESPONSE_PATH = "/SAML2/SSO/Response";
    }

    interface TranslatorUrls {
        String TRANSLATOR_ROOT = "/translator";
        String TRANSLATE_HUB_RESPONSE_PATH = "/translate-hub-response";
    }

    interface VerifyServiceProviderUrls {
        String TRANSLATE_HUB_RESPONSE_ENDPOINT = "/translate-response";
        String GENERATE_HUB_AUTHN_REQUEST_ENDPOINT = "/generate-request";
    }
}
