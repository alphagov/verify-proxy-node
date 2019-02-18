package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class EidasSamlParserClientRule<T> extends DropwizardClientRule {
    public EidasSamlParserClientRule(T resource) {
        super(resource);
    }
}