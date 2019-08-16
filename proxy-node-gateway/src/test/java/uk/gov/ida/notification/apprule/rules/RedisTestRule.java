package uk.gov.ida.notification.apprule.rules;

import org.junit.rules.ExternalResource;
import redis.embedded.Redis;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;
import redis.embedded.exceptions.EmbeddedRedisException;
import redis.embedded.util.Architecture;
import redis.embedded.util.OS;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class RedisTestRule extends ExternalResource {
    private Redis redis;

    public RedisTestRule(int port) {
        File redisServerMac;
        File redisServerTrusty;
        try {
            URL redisServerMacResource = RedisTestRule.class.getResource("/redis-server-mac-5.0.3");
            redisServerMac = Paths.get(redisServerMacResource.toURI()).toFile();

            URL redisServerTrustyResource = RedisTestRule.class.getResource("/redis-server-trusty-5.0.3");
            redisServerTrusty = Paths.get(redisServerTrustyResource.toURI()).toFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }

        RedisExecProvider customProvider = RedisExecProvider.defaultProvider()
                .override(OS.UNIX, redisServerTrusty.getAbsolutePath())
                .override(OS.MAC_OS_X, Architecture.x86_64, redisServerMac.getAbsolutePath());

        try {
            redis = new RedisServer(customProvider, port);
        } catch (IOException e) {
            throw new EmbeddedRedisException(e.getMessage());
        }

        try {
            this.before();
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void before() throws Throwable {
        if (!redis.isActive()) {
            redis.start();
        }
        super.before();
    }

    @Override
    protected void after() {
        redis.stop();
        super.after();
    }
}
