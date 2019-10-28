package uk.gov.ida.notification.translator;

import engineering.reliability.gds.metrics.bundle.PrometheusBundle;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.configuration.CredentialConfiguration;
import uk.gov.ida.notification.exceptions.mappers.ApplicationExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.CatchAllExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.JsonErrorResponseRuntimeExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.JsonErrorResponseValidationExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.shared.istio.IstioHeaderMapperFilter;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;
import uk.gov.ida.notification.translator.configuration.TranslatorConfiguration;
import uk.gov.ida.notification.translator.resources.HubResponseTranslatorResource;
import uk.gov.ida.notification.translator.saml.EidasFailureResponseGenerator;
import uk.gov.ida.notification.translator.saml.EidasResponseGenerator;
import uk.gov.ida.notification.translator.saml.HubResponseTranslator;

public class TranslatorApplication extends Application<TranslatorConfiguration> {

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            String configFile = System.getenv("CONFIG_FILE");

            if (configFile == null) {
                throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
            }

            new TranslatorApplication().run("server", configFile);
        } else {
            new TranslatorApplication().run(args);
        }
    }

    @Override
    public String getName() {
        return "translator";
    }

    @Override
    public void initialize(final Bootstrap<TranslatorConfiguration> bootstrap) {
        // Needed to correctly interpolate environment variables in config file
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        // Needed to initialise OpenSAML libraries
        // The eidas-opensaml3 library provides its own initializer that will be executed
        // by the InitializationService
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException(e);
        }

        VerifySamlInitializer.init();

        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new LogstashBundle());
        bootstrap.addBundle(new PrometheusBundle());
    }

    @Override
    public void run(final TranslatorConfiguration configuration, final Environment environment) {
        environment.jersey().register(IstioHeaderMapperFilter.class);
        environment.jersey().register(ProxyNodeLoggingFilter.class);
        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("translator");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);

        registerExceptionMappers(environment);
        registerResources(configuration, environment);
        registerInjections(environment);

    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new ApplicationExceptionMapper());
        environment.jersey().register(new JsonErrorResponseValidationExceptionMapper());
        environment.jersey().register(new JsonErrorResponseRuntimeExceptionMapper());
        environment.jersey().register(new CatchAllExceptionMapper());
    }

    private void registerResources(TranslatorConfiguration configuration, Environment environment) {
        final EidasResponseGenerator eidasResponseGenerator = createEidasResponseGenerator(configuration);
        final VerifyServiceProviderProxy vspProxy = configuration.getVspConfiguration().buildVerifyServiceProviderProxy(environment);
        final HubResponseTranslatorResource hubResponseTranslatorResource = new HubResponseTranslatorResource(eidasResponseGenerator, vspProxy);

        environment.jersey().register(hubResponseTranslatorResource);
    }

    private EidasResponseGenerator createEidasResponseGenerator(TranslatorConfiguration configuration) {
        final HubResponseTranslator hubResponseTranslator = new HubResponseTranslator(
                EidasResponseBuilder::instance,
                configuration.getConnectorNodeIssuerId(),
                configuration.getProxyNodeMetadataForConnectorNodeUrl().toString(),
                configuration.getConnectorNodeNationalityCode()
        );

        final EidasFailureResponseGenerator failureResponseGenerator = new EidasFailureResponseGenerator(
                EidasResponseBuilder::instance,
                configuration.getConnectorNodeIssuerId(),
                configuration.getProxyNodeMetadataForConnectorNodeUrl().toString()
        );

        final CredentialConfiguration credentialConfiguration = configuration.getCredentialConfiguration();
        final SamlObjectSigner samlObjectSigner = new SamlObjectSigner(credentialConfiguration.getCredential(),
                                                                       credentialConfiguration.getAlgorithm(),
                                                                       credentialConfiguration.getKeyHandle());

        return new EidasResponseGenerator(hubResponseTranslator, failureResponseGenerator, samlObjectSigner);
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
