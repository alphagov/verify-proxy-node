package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import org.eclipse.jetty.server.session.SessionHandler;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.notification.configuration.RedisServiceConfiguration;
import uk.gov.ida.notification.exceptions.mappers.ErrorPageExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.ExceptionToSamlErrorResponseMapper;
import uk.gov.ida.notification.exceptions.mappers.GenericExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.MissingMetadataExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.proxy.EidasSamlParserProxy;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.session.storage.InMemoryStorage;
import uk.gov.ida.notification.session.storage.RedisStorage;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.istio.IstioHeaderMapperFilter;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter;
import uk.gov.ida.notification.shared.metadata.MetadataPublishingBundle;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;

import javax.servlet.DispatcherType;
import java.net.URI;
import java.util.EnumSet;

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
        bootstrap.addBundle(new MetadataPublishingBundle<>(GatewayConfiguration::getMetadataPublishingConfiguration));
    }

    @Override
    public void run(final GatewayConfiguration configuration,
                    final Environment environment) {

        final ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("gateway");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);

        final RedisServiceConfiguration redisService = configuration.getRedisService();

        final SessionStore sessionStorage = redisService.isLocal() ?
                new InMemoryStorage() : new RedisStorage(redisService);

        final SamlFormViewBuilder samlFormViewBuilder = new SamlFormViewBuilder();

        final TranslatorProxy translatorProxy = configuration
                .getTranslatorServiceConfiguration()
                .buildTranslatorProxy(environment);


        registerProviders(environment);
        registerResources(configuration, environment, samlFormViewBuilder, translatorProxy, sessionStorage);
        registerExceptionMappers(environment, samlFormViewBuilder, translatorProxy, sessionStorage, configuration.getErrorPageRedirectUrl());
        registerInjections(environment);
    }

    private void registerProviders(Environment environment) {
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSessionCookie("gateway-session");
        environment.servlets().setSessionHandler(sessionHandler);
        setRequestServletFilter(environment);
        setResponseServletFilter(environment);
        environment.jersey().register(IstioHeaderMapperFilter.class);
        environment.jersey().register(ProxyNodeLoggingFilter.class);
    }

    private void setRequestServletFilter(Environment environment) {
        JourneyIdGeneratingServletFilter requestFilter = new JourneyIdGeneratingServletFilter(new SecureRandomIdentifierGenerationStrategy());
        environment.servlets()
                .addFilter(requestFilter.getClass().getSimpleName(), requestFilter)
                .addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST),
                        true,
                        Urls.GatewayUrls.GATEWAY_ROOT + Urls.GatewayUrls.GATEWAY_EIDAS_AUTHN_REQUEST_POST_PATH,
                        Urls.GatewayUrls.GATEWAY_ROOT + Urls.GatewayUrls.GATEWAY_EIDAS_AUTHN_REQUEST_REDIRECT_PATH);
    }

    private void setResponseServletFilter(Environment environment) {
        JourneyIdHubResponseServletFilter responseFilter = new JourneyIdHubResponseServletFilter();
        environment.servlets()
                .addFilter(responseFilter.getClass().getSimpleName(), responseFilter)
                .addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST),
                        true,
                        Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE);
    }

    private void registerExceptionMappers(
            Environment environment,
            SamlFormViewBuilder samlFormViewBuilder,
            TranslatorProxy translatorProxy,
            SessionStore sessionStore,
            URI errorPageRedirectUrl) {

        environment.jersey().register(new MissingMetadataExceptionMapper());
        environment.jersey().register(new ExceptionToSamlErrorResponseMapper(samlFormViewBuilder, translatorProxy, sessionStore));
        environment.jersey().register(new ErrorPageExceptionMapper(errorPageRedirectUrl));
        environment.jersey().register(new GenericExceptionMapper(errorPageRedirectUrl));
    }

    private void registerResources(
            GatewayConfiguration configuration,
            Environment environment,
            SamlFormViewBuilder samlFormViewBuilder,
            TranslatorProxy translatorProxy,
            SessionStore sessionStorage) {

        EidasSamlParserProxy espProxy = configuration
                .getEidasSamlParserServiceConfiguration()
                .buildEidasSamlParserService(environment);

        VerifyServiceProviderProxy vspProxy = configuration
                .getVerifyServiceProviderConfiguration()
                .buildVerifyServiceProviderProxy(environment);

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

    private void registerInjections(Environment environment) {
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(IstioHeaderStorage.class);
            }
        });
    }
}
