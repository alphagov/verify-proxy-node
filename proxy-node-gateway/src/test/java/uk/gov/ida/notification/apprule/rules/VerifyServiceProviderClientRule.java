package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class VerifyServiceProviderClientRule<T> extends DropwizardClientRule {
    public VerifyServiceProviderClientRule(T resource) {
        super(resource);
    }
}