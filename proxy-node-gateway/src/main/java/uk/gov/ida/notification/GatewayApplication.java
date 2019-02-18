package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.notification.exceptions.mappers.EidasSamlParserResponseExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.GenericExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.VerifyServiceProviderResponseExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.proxy.EidasSamlParserProxy;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.resources.HubResponseResource;
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
    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new EidasSamlParserResponseExceptionMapper());
        environment.jersey().register(new VerifyServiceProviderResponseExceptionMapper());
        environment.jersey().register(new GenericExceptionMapper());
    }

    private void registerResources(GatewayConfiguration configuration, Environment environment) {
        SamlFormViewBuilder samlFormViewBuilder = new SamlFormViewBuilder();

        EidasSamlParserProxy espProxy = configuration
            .getEidasSamlParserServiceConfiguration()
            .buildEidasSamlParserService(environment);

        VerifyServiceProviderProxy vspProxy = configuration
            .getVerifyServiceProviderConfiguration()
            .buildVerifyServiceProviderProxy(environment);

        environment.jersey().register(new EidasAuthnRequestResource(
                espProxy,
                vspProxy,
                samlFormViewBuilder));

        TranslatorProxy translatorProxy = configuration
            .getTranslatorServiceConfiguration()
            .buildTranslatorProxy(environment);

        environment.jersey().register(new HubResponseResource(
                samlFormViewBuilder,
                translatorProxy
        ));
    }
}
