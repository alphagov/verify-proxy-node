package uk.gov.ida.notification;

import io.prometheus.client.Counter;

public class Metrics {

    private static final String LABEL_PREFIX = "verify_proxy_node";

    public static final Counter REQUESTS = Counter.build(
            LABEL_PREFIX + "_requests",
            "Number of eIDAS SAML Requests To Verify Proxy Node ")
            .register();

    public static final Counter REQUESTS_SUCCESSFUL = Counter.build(
            LABEL_PREFIX + "_requests_successful",
            "Number of Successful eIDAS SAML Requests To Verify Proxy Node")
            .register();

    public static final Counter RESPONSES = Counter.build(
            LABEL_PREFIX + "_responses",
            "Number of eIDAS SAML Responses To Verify Proxy Node")
            .register();

    public static final Counter RESPONSES_SUCCESSFUL = Counter.build(
            LABEL_PREFIX + "_responses_successful",
            "Number of Successful eIDAS SAML Responses To Verify Proxy Node")
            .register();

}
