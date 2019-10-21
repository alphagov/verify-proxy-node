package uk.gov.ida.notification;

import io.prometheus.client.Counter;

public class MetricsUtils {

    private static final String LABEL_PREFIX = "verify_proxy_node";

    public static final Counter REQUESTS = Counter.build(
            LABEL_PREFIX + "_requests_total",
            "Number of eIDAS SAML requests to Verify Proxy Node ")
            .register();

    public static final Counter REQUESTS_SUCCESSFUL = Counter.build(
            LABEL_PREFIX + "_successful_requests_total",
            "Number of successful eIDAS SAML requests to Verify Proxy Node")
            .register();

    public static final Counter RESPONSES = Counter.build(
            LABEL_PREFIX + "_responses_total",
            "Number of eIDAS SAML responses to Verify Proxy Node")
            .register();

    public static final Counter RESPONSES_SUCCESSFUL = Counter.build(
            LABEL_PREFIX + "_successful_responses_total",
            "Number of successful eIDAS SAML responses To Verify Proxy Node")
            .register();

}
