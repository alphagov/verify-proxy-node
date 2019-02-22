package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class TranslatorClientRule<T> extends DropwizardClientRule {
    public TranslatorClientRule(T resource) { super(resource); }
}
