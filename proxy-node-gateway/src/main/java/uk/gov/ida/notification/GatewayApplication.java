package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.notification.configuration.RedisServiceConfiguration;
import uk.gov.ida.notification.exceptions.mappers.EidasSamlParserResponseExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.GenericExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.SessionAlreadyExistsExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.SessionAttributeExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.SessionMissingExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.TranslatorResponseExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.VspGenerateAuthnRequestResponseExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.proxy.EidasSamlParserProxy;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.session.storage.InMemoryStorage;
import uk.gov.ida.notification.session.storage.RedisStorage;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.shared.IstioHeaderMapperFilter;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;

public class GatewayApplication extends Application<GatewayConfiguration> {

    @SuppressWarnings("WeakerAccess") // Needed for DropwizardAppRules
    public GatewayApplication() {
    }

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            String configFile = System.getenv("CONFIG_FILE");

            if (configFile == null) {
                throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
            }

            new GatewayApplication().run("server", configFile);
        } else {
            new GatewayApplication().run(args);
        }
    }

    @Override
    public String getName() {
        return "EidasProxyNode";
    }

    @Override
    public void initialize(final Bootstrap<GatewayConfiguration> bootstrap) {
        // Needed to correctly interpolate environment variables in config file
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new LogstashBundle());
    }

    @Override
    public void run(final GatewayConfiguration configuration,
                    final Environment environment) {

        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("gateway");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);

        registerProviders(environment);
        registerExceptionMappers(environment);
        registerResources(configuration, environment);
    }

    private void registerProviders(Environment environment) {
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSessionCookie("gateway-session");
        environment.servlets().setSessionHandler(sessionHandler);

        environment.jersey().register(IstioHeaderMapperFilter.class);
    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new EidasSamlParserResponseExceptionMapper());
        environment.jersey().register(new VspGenerateAuthnRequestResponseExceptionMapper());
        environment.jersey().register(new TranslatorResponseExceptionMapper());
        environment.jersey().register(new SessionAttributeExceptionMapper());
        environment.jersey().register(new SessionMissingExceptionMapper());
        environment.jersey().register(new SessionAlreadyExistsExceptionMapper());
        environment.jersey().register(new GenericExceptionMapper());
    }

    private void registerResources(GatewayConfiguration configuration, Environment environment) {


        RedisServiceConfiguration redisService = configuration.getRedisService();

        SessionStore sessionStorage = redisService.isLocal() ?
                new InMemoryStorage() : new RedisStorage(redisService);

        SamlFormViewBuilder samlFormViewBuilder = new SamlFormViewBuilder();

        EidasSamlParserProxy espProxy = configuration
            .getEidasSamlParserServiceConfiguration()
            .buildEidasSamlParserService(environment);

        VerifyServiceProviderProxy vspProxy = configuration
            .getVerifyServiceProviderConfiguration()
            .buildVerifyServiceProviderProxy(environment);

        TranslatorProxy translatorProxy = configuration
            .getTranslatorServiceConfiguration()
            .buildTranslatorProxy(environment);


        environment.lifecycle().manage(sessionStorage);

        environment.jersey().register(new EidasAuthnRequestResource(
                espProxy,
                vspProxy,
                samlFormViewBuilder,
                sessionStorage));

        environment.jersey().register(new HubResponseResource(
                samlFormViewBuilder,
                translatorProxy,
                sessionStorage
        ));
    }
}
