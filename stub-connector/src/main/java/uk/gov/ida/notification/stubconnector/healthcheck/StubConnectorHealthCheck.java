package uk.gov.ida.notification.stubconnector.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public class StubConnectorHealthCheck extends HealthCheck {

    public String getName() {
        return "stub-connector";
    }

    @Override
    protected Result check() {
        return Result.healthy();
    }

}
