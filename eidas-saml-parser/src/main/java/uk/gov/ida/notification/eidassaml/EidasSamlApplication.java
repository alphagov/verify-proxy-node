package uk.gov.ida.notification.eidassaml;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.eidassaml.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.AssertionConsumerServiceValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.ComparisonValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.RequestedAttributesValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.SpTypeValidator;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.deprecate.DestinationValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

import static uk.gov.ida.notification.saml.SamlSignatureValidatorFactory.createSamlRequestSignatureValidator;

public class EidasSamlApplication extends Application<EidasSamlConfiguration> {

    private MetadataResolverBundle<EidasSamlConfiguration> connectorMetadataResolverBundle;

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            String configFile = System.getenv("CONFIG_FILE");

            if (configFile == null) {
                throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
            }
            new EidasSamlApplication().run("server", configFile);
        } else {
            new EidasSamlApplication().run(args);
        }
    }

    public void initialize(final Bootstrap<EidasSamlConfiguration> bootstrap) {

        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException(e);
        }

        VerifySamlInitializer.init();


        connectorMetadataResolverBundle = new MetadataResolverBundle<>(EidasSamlConfiguration::getConnectorMetadataConfiguration);
        
        bootstrap.addBundle(connectorMetadataResolverBundle);
        bootstrap.addBundle(new LogstashBundle());
    }

    @Override
    public void run(EidasSamlConfiguration configuration, Environment environment) throws Exception {
        EidasAuthnRequestValidator eidasAuthnRequestValidator = createEidasAuthnRequestValidator(configuration, connectorMetadataResolverBundle);
        SamlRequestSignatureValidator samlRequestSignatureValidator = createSamlRequestSignatureValidator(connectorMetadataResolverBundle);

        environment.jersey().register(new EidasSamlResource(eidasAuthnRequestValidator, samlRequestSignatureValidator));
    }

    private EidasAuthnRequestValidator createEidasAuthnRequestValidator(EidasSamlConfiguration configuration, MetadataResolverBundle hubMetadataResolverBundle) throws Exception {
        MessageReplayChecker replayChecker = configuration.getReplayChecker().createMessageReplayChecker("eidas-saml-parser");
        DestinationValidator destinationValidator = new DestinationValidator(
                configuration.getProxyNodeAuthnRequestUrl(), configuration.getProxyNodeAuthnRequestUrl().getPath());

        return new EidasAuthnRequestValidator(
                new RequestIssuerValidator(),
                new SpTypeValidator(),
                new LoaValidator(),
                new RequestedAttributesValidator(),
                replayChecker,
                new ComparisonValidator(),
                destinationValidator,
                new AssertionConsumerServiceValidator(hubMetadataResolverBundle.getMetadataResolver())
        );
    }
}
