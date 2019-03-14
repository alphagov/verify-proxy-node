package uk.gov.ida.notification.apprule.rules;

import org.junit.rules.ExternalResource;
import redis.embedded.Redis;
import redis.embedded.RedisServer;

public class RedisTestRule extends ExternalResource {
    private Redis redis;

    public RedisTestRule(int port) {
        redis = new RedisServer(port);
    }

    @Override
    protected void before() throws Throwable {
        redis.start();
        super.before();
    }

    @Override
    protected void after() {
        redis.stop();
        super.after();
    }
}
