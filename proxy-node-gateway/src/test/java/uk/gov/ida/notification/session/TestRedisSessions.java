package uk.gov.ida.notification.session;

import com.github.fppt.jedismock.RedisServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.notification.configuration.RedisServiceConfiguration;
import uk.gov.ida.notification.exceptions.SessionAlreadyExistsException;
import uk.gov.ida.notification.exceptions.SessionMissingException;
import uk.gov.ida.notification.session.storage.RedisStorage;
import uk.gov.ida.notification.session.storage.SessionStore;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestRedisSessions {

    private static final String TEST_KEY = "TEST_KEY";
    private static final GatewaySessionData TEST_DATA = populateTestData();

    @Mock
    private static RedisServiceConfiguration redisServiceConfiguration;

    private RedisServer server = null;
    private SessionStore sessionStore = null;

    @Before
    public void before() throws Exception {
        server = RedisServer.newRedisServer();
        server.start();

        URI redisURI = new URI("redis://" + server.getHost() + ":" + server.getBindPort() + "/");

        when(redisServiceConfiguration.getUrl()).thenReturn(redisURI);
        when(redisServiceConfiguration.getRecordTTL()).thenReturn(Long.valueOf(1000));

        sessionStore = new RedisStorage(redisServiceConfiguration);
        sessionStore.start();
    }

    @After
    public void after() throws Exception {
        sessionStore.stop();
        server.stop();
        server = null;
    }

    @Test
    public void addSessionToRedisAndRetrieve() {
        sessionStore.addSession(TEST_KEY, TEST_DATA);

        GatewaySessionData session = sessionStore.getSession(TEST_KEY);

        assertThat(session).isEqualToComparingFieldByField(TEST_DATA);
    }

    @Test
    public void testRedisContainsFunction() {
        assertThat(sessionStore.sessionExists(TEST_KEY)).isFalse();

        sessionStore.addSession(TEST_KEY, TEST_DATA);

        assertThat(sessionStore.sessionExists(TEST_KEY)).isTrue();
    }

    @Test(expected = SessionAlreadyExistsException.class)
    public void addSessionAlreadyExistsWithKey() {
        sessionStore.addSession(TEST_KEY, TEST_DATA);
        sessionStore.addSession(TEST_KEY, TEST_DATA);
    }

    @Test(expected = SessionMissingException.class)
    public void getSessionButNoKeyExists() {
        sessionStore.getSession(TEST_KEY);
    }

    private static GatewaySessionData populateTestData() {
        return new GatewaySessionData("aHubRequestId",
                "anEidasRequestId",
                "anEidasDestination",
                "anEidasRelayState",
                "eidasIssuer");
    }
}
