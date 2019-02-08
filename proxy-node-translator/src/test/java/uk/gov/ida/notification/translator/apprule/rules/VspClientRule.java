package uk.gov.ida.notification.translator.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class VspClientRule extends DropwizardClientRule {
    public VspClientRule() {
        super(new StubVspResource());
    }
}
