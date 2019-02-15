package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class EidasSamlParserClientRule extends DropwizardClientRule {
    public EidasSamlParserClientRule() {
        super(new TestEidasSamlResource());
    }
}