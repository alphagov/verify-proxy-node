package uk.gov.ida.notification.healthcheck;

import com.codahale.metrics.health.HealthCheck;

import javax.inject.Inject;
import javax.inject.Named;

public class ProxyNodeHealthCheck extends HealthCheck {

    private String serviceName;

    @Inject
    public ProxyNodeHealthCheck(@Named("ServiceName") String serviceName) {
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
