package uk.gov.ida.notification.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public class GatewayHealthCheck extends HealthCheck {

    public String getName() {
        return "gateway";
    }

    @Override
    protected Result check() {
        return Result.healthy();
    }
}
