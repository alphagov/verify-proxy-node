package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.pki.CredentialBuilder;
import uk.gov.ida.notification.pki.DecryptingCredential;
import uk.gov.ida.notification.pki.SigningCredential;
import uk.gov.ida.notification.resources.ConnectorNodeMetadataResource;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.resources.HubMetadataResource;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.saml.translation.HubResponseTranslator;
import uk.gov.ida.stubs.resources.StubConnectorNodeResource;

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

        // Verify SAML
        VerifySamlInitializer.init();

        // Views
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(final EidasProxyNodeConfiguration configuration,
                    final Environment environment) {
        String connectorNodeUrl = configuration.getConnectorNodeUrl().toString();

        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator(
                configuration.getProxyNodeEntityId(),
                connectorNodeUrl
        );
        EidasAuthnRequestTranslator eidasAuthnRequestTranslator = new EidasAuthnRequestTranslator(
                configuration.getProxyNodeEntityId(),
                configuration.getHubUrl().toString());
        SigningCredential hubFacingSigningCredential = CredentialBuilder
                .withKeyPairConfiguration(configuration.getHubFacingSigningKeyPair())
                .buildSigningCredential();
        SamlObjectSigner hubAuthnRequestSigner = new SamlObjectSigner(hubFacingSigningCredential);
        HubAuthnRequestGenerator hubAuthnRequestGenerator = new HubAuthnRequestGenerator(
                eidasAuthnRequestTranslator,
                hubAuthnRequestSigner);
        SamlFormViewBuilder samlFormViewBuilder = new SamlFormViewBuilder();
        DecryptingCredential hubFacingDecryptingCredential = CredentialBuilder
                .withKeyPairConfiguration(configuration.getHubFacingEncryptionKeyPair())
                .buildDecryptingCredential();
        ResponseAssertionDecrypter assertionDecrypter = new ResponseAssertionDecrypter(hubFacingDecryptingCredential);

        environment.jersey().register(AuthnRequestParameterProvider.class);
        environment.jersey().register(ResponseParameterProvider.class);
        environment.jersey().register(new EidasAuthnRequestResource(
                configuration,
                hubAuthnRequestGenerator,
                samlFormViewBuilder
        ));
        environment.jersey().register(new HubResponseResource(hubResponseTranslator, samlFormViewBuilder, assertionDecrypter, connectorNodeUrl));
        environment.jersey().register(new HubMetadataResource());
        environment.jersey().register(new ConnectorNodeMetadataResource());
        environment.jersey().register(new StubConnectorNodeResource());
    }
}
