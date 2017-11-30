package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.saml.translation.HubResponseTranslator;
import uk.gov.ida.stubs.resources.StubConnectorNodeResource;
import uk.gov.ida.stubs.resources.StubIdpResource;

import javax.xml.parsers.ParserConfigurationException;

public class EidasProxyNodeApplication extends Application<EidasProxyNodeConfiguration> {

    @SuppressWarnings("WeakerAccess") // Needed for DropwizardAppRules
    public EidasProxyNodeApplication() {
    }

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            String configFile = System.getenv("CONFIG_FILE");

            if (configFile == null) {
                throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
            }

            new EidasProxyNodeApplication().run("server", configFile);
        } else {
            new EidasProxyNodeApplication().run(args);
        }
    }

    @Override
    public String getName() {
        return "EidasProxyNode";
    }

    @Override
    public void initialize(final Bootstrap<EidasProxyNodeConfiguration> bootstrap) {
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
        } catch(InitializationException e) {
            throw new RuntimeException(e);
        }

        // Views
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(final EidasProxyNodeConfiguration configuration,
                    final Environment environment) throws ParserConfigurationException {

        SamlParser samlParser = new SamlParser();
        SamlMarshaller samlMarshaller = new SamlMarshaller();
        EidasAuthnRequestMapper eidasAuthnRequestMapper = new EidasAuthnRequestMapper(samlParser);
        EidasAuthnRequestTranslator eidasAuthnRequestTranslator = new EidasAuthnRequestTranslator(
                configuration.getProxyNodeEntityId(),
                configuration.getHubUrl().toString());
        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator(
                configuration.getProxyNodeEntityId(),
                configuration.getConnectorNodeUrl().toString(),
                samlParser,
                samlMarshaller
        );
        SamlFormViewMapper samlFormViewMapper = new SamlFormViewMapper(samlMarshaller);

        environment.jersey().register(new EidasAuthnRequestResource(
                configuration,
                eidasAuthnRequestTranslator,
                samlFormViewMapper,
                eidasAuthnRequestMapper));
        environment.jersey().register(new HubResponseResource(configuration, hubResponseTranslator));
        environment.jersey().register(new StubConnectorNodeResource());
        environment.jersey().register(new StubIdpResource());
    }
}
