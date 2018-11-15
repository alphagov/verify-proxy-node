package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class TranslatorClientRule extends DropwizardClientRule {
    public TranslatorClientRule() {
        super(new TestTranslatorResource());
    }
}
