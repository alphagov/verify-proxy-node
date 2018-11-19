package uk.gov.ida.notification.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public class ProxyNodeHealthCheck extends HealthCheck {

    private String serviceName;

    public ProxyNodeHealthCheck(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getName() {
        return serviceName;
    }

    @Override
    protected Result check() {
        return Result.healthy();
    }
}
