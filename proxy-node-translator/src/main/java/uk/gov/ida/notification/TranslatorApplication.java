package uk.gov.ida.notification;

import com.google.common.collect.ImmutableList;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import uk.gov.ida.bundles.LoggingBundle;
import uk.gov.ida.notification.exceptions.mappers.HubResponseExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.resources.HubResponseFromGatewayResource;
import uk.gov.ida.notification.saml.HubResponseTranslator;
import uk.gov.ida.notification.saml.ResponseAssertionFactory;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.deprecate.DestinationValidator;
import uk.gov.ida.notification.saml.deprecate.EncryptedResponseFromIdpValidator;
import uk.gov.ida.notification.saml.deprecate.IdpResponseValidator;
import uk.gov.ida.notification.saml.deprecate.SamlStatusToIdaStatusCodeMapper;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.validation.HubResponseValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.ResponseAttributesValidator;
import uk.gov.ida.notification.signing.KeyRetrieverService;
import uk.gov.ida.notification.signing.KeyRetrieverServiceFactory;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.net.URI;
import java.util.List;

import static uk.gov.ida.notification.saml.SamlSignatureValidatorFactory.createSamlMessageSignatureValidator;

public class TranslatorApplication extends Application<TranslatorConfiguration> {

    private Metadata connectorMetadata;

    private MetadataResolverBundle<TranslatorConfiguration> hubMetadataResolverBundle;
    private MetadataResolverBundle<TranslatorConfiguration> connectorMetadataResolverBundle;


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
        } catch(InitializationException e) {
            throw new RuntimeException(e);
        }

        VerifySamlInitializer.init();

        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new LoggingBundle());


        // Metadata
        hubMetadataResolverBundle = new MetadataResolverBundle<>(TranslatorConfiguration::getHubMetadataConfiguration);
        bootstrap.addBundle(hubMetadataResolverBundle);

        connectorMetadataResolverBundle = new MetadataResolverBundle<>(TranslatorConfiguration::getConnectorMetadataConfiguration);
        bootstrap.addBundle(connectorMetadataResolverBundle);

    }

    @Override
    public void run(final TranslatorConfiguration configuration,
                    final Environment environment) throws Exception {

        connectorMetadata = new Metadata(connectorMetadataResolverBundle.getMetadataCredentialResolver());

        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("translator");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);


        registerProviders(environment);
        registerExceptionMappers(environment);
        registerResources(configuration, environment);

    }

    private void registerProviders(Environment environment) {
        environment.jersey().register(ResponseParameterProvider.class);
    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new HubResponseExceptionMapper());
    }

    private void registerResources(TranslatorConfiguration configuration, Environment environment) throws Exception {
        EidasResponseGenerator eidasResponseGenerator = createEidasResponseGenerator(configuration);

        HubResponseValidator hubResponseValidator = createHubResponseValidator(configuration);


        environment.jersey().register(new HubResponseFromGatewayResource(
                eidasResponseGenerator,
                configuration.getConnectorNodeUrl().toString(),
                configuration.getConnectorMetadataConfiguration().getExpectedEntityId(),
                connectorMetadata,
                hubResponseValidator));
    }

    private EidasResponseGenerator createEidasResponseGenerator(TranslatorConfiguration configuration) {
        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator(
                configuration.getConnectorNodeIssuerId(),
                configuration.getConnectorNodeUrl().toString(),
                configuration.getProxyNodeMetadataForConnectorNodeUrl().toString()
        );
        KeyRetrieverService keyRetrieverService = KeyRetrieverServiceFactory.createKeyRetrieverService(configuration);
        return new EidasResponseGenerator(hubResponseTranslator, keyRetrieverService.createSamlObjectSigner());

    }

    private HubResponseValidator createHubResponseValidator(TranslatorConfiguration configuration) throws Exception {
        URI proxyNodeResponseUrl = configuration.getProxyNodeResponseUrl();
        String proxyNodeEntityId = configuration.getProxyNodeEntityId();

        SamlMessageSignatureValidator hubResponseMessageSignatureValidator = createSamlMessageSignatureValidator(hubMetadataResolverBundle);

        MessageReplayChecker messageReplayChecker = configuration
            .getReplayChecker()
            .createMessageReplayChecker("translator-hub");

        IdpResponseValidator idpResponseValidator = new IdpResponseValidator(
                new SamlResponseSignatureValidator(hubResponseMessageSignatureValidator),
                createDecrypter(configuration.getHubFacingEncryptionKeyPair()),
                new SamlAssertionsSignatureValidator(hubResponseMessageSignatureValidator),
                new EncryptedResponseFromIdpValidator<>(new SamlStatusToIdaStatusCodeMapper()),
                new DestinationValidator(proxyNodeResponseUrl, proxyNodeResponseUrl.getPath()),
                ResponseAssertionFactory.createResponseAssertionsFromIdpValidator("Translator", proxyNodeEntityId, messageReplayChecker)
        );
        ResponseAttributesValidator responseAttributesValidator = new ResponseAttributesValidator();
        return new HubResponseValidator(idpResponseValidator, responseAttributesValidator, new LoaValidator());
    }

    private AssertionDecrypter createDecrypter(KeyPairConfiguration configuration) {
        BasicCredential decryptionCredential = new BasicCredential(
                configuration.getPublicKey().getPublicKey(),
                configuration.getPrivateKey().getPrivateKey()
        );
        List<Credential> decryptionCredentials = ImmutableList.of(decryptionCredential);
        Decrypter decrypter = new DecrypterFactory().createDecrypter(decryptionCredentials);
        EncryptionAlgorithmValidator encryptionAlgorithmValidator = new EncryptionAlgorithmValidator();
        return new AssertionDecrypter(
                encryptionAlgorithmValidator,
                decrypter
        );
    }
}
