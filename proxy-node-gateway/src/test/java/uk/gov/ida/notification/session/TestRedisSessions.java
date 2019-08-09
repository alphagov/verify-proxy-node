package uk.gov.ida.notification.session;

import com.github.fppt.jedismock.RedisServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.notification.configuration.RedisServiceConfiguration;
import uk.gov.ida.notification.exceptions.SessionAlreadyExistsException;
import uk.gov.ida.notification.exceptions.SessionMissingException;
import uk.gov.ida.notification.session.storage.RedisStorage;
import uk.gov.ida.notification.session.storage.SessionStore;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestRedisSessions {

    private static RedisServer server = null;
    private SessionStore sessionStore = null;

    private RedisServiceConfiguration redisServiceConfiguration;


    private GatewaySessionData testData = populateTestData();

    private static final String TEST_KEY = "TEST_KEY";

    @Before
    public void before() throws Exception {
        redisServiceConfiguration = mock(RedisServiceConfiguration.class);

        server = RedisServer.newRedisServer();
        server.start();

        URI redisURI = new URI("redis://" + server.getHost() + ":" + server.getBindPort() + "/");

        when(redisServiceConfiguration.getUrl()).thenReturn(redisURI);
        when(redisServiceConfiguration.getRecordTTL()).thenReturn(Long.valueOf(1000));

        sessionStore = new RedisStorage(redisServiceConfiguration);
        sessionStore.start();
    }

    @Test
    public void addSessionToRedisAndRetrieve() {
        sessionStore.addSession(TEST_KEY, testData);

        GatewaySessionData session = sessionStore.getSession(TEST_KEY);

        assertThat(session).isEqualToComparingFieldByField(testData);
    }

    @Test
    public void testRedisContainsFunction() {
        assertThat(sessionStore.sessionExists(TEST_KEY)).isFalse();

        sessionStore.addSession(TEST_KEY, testData);

        assertThat(sessionStore.sessionExists(TEST_KEY)).isTrue();
    }

    @Test(expected = SessionAlreadyExistsException.class)
    public void addSessionAlreadyExistsWithKey() {
        sessionStore.addSession(TEST_KEY, testData);
        sessionStore.addSession(TEST_KEY, testData);
    }

    @Test(expected = SessionMissingException.class)
    public void getSessionButNoKeyExists() {
        sessionStore.getSession(TEST_KEY);
    }

    @After
    public void after() throws Exception {
        sessionStore.stop();
        server.stop();
        server = null;
    }

    private GatewaySessionData populateTestData() {
        return new GatewaySessionData("aHubRequestId",
                                      "anEidasRequestId",
                                      "anEidasDestination",
                                      "anEidasConnectorPublicKey",
                                      "anEidasRelayState",
                                        "an entity id");
    }
}
