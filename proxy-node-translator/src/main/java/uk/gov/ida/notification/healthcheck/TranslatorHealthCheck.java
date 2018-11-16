package uk.gov.ida.notification.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public class TranslatorHealthCheck extends HealthCheck {

    public String getName() {
        return "translator";
    }

    @Override
    protected Result check() {
        return Result.healthy();
    }
}
