package uk.gov.ida.notification;

import io.prometheus.client.Counter;

public class Metrics {

    public static final String RELEASE_NAME = getReleaseName();

    private static final String RELEASE_NAME_LABEL = "release";
    private static final String LABEL_PREFIX = "verify_proxy_node";

    public static final Counter REQUESTS = Counter.build(
            LABEL_PREFIX + "_requests",
            "Number of eIDAS SAML Requests To Verify Proxy Node ")
            .labelNames(RELEASE_NAME_LABEL)
            .register();

    public static final Counter REQUESTS_SUCCESSFUL = Counter.build(
            LABEL_PREFIX + "_requests_successful",
            "Number of Successful eIDAS SAML Requests To Verify Proxy Node")
            .labelNames(RELEASE_NAME_LABEL)
            .register();

    public static final Counter RESPONSES = Counter.build(
            LABEL_PREFIX + "_responses",
            "Number of eIDAS SAML Responses To Verify Proxy Node")
            .labelNames(RELEASE_NAME_LABEL)
            .register();

    public static final Counter RESPONSES_SUCCESSFUL = Counter.build(
            LABEL_PREFIX + "_responses_successful",
            "Number of Successful eIDAS SAML Responses To Verify Proxy Node")
            .labelNames(RELEASE_NAME_LABEL)
            .register();

    static { // initialise each label so first increment is not missed
        REQUESTS.labels(RELEASE_NAME_LABEL);
        REQUESTS_SUCCESSFUL.labels(RELEASE_NAME_LABEL);
        RESPONSES.labels(RELEASE_NAME_LABEL);
        RESPONSES_SUCCESSFUL.labels(RELEASE_NAME_LABEL);
    }

    private static String getReleaseName() {
        String value = System.getenv("RELEASE_NAME");
        if (value != null && !value.isBlank()) {
            return value;
        } else {
            return "none";
        }
    }
}
