package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class ProxyNodeClientRule extends DropwizardClientRule {
    public ProxyNodeClientRule() {
        super(new TestProxyNodeResource());
    }
}
