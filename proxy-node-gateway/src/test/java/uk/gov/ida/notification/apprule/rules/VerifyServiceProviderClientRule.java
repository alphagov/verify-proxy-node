package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class VerifyServiceProviderClientRule extends DropwizardClientRule {
    public VerifyServiceProviderClientRule() {
        super(new TestVerifyServiceProviderResource());
    }
}