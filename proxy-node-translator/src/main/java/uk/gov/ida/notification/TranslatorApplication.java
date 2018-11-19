package uk.gov.ida.notification;

import com.google.common.collect.ImmutableList;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.exceptions.mappers.AuthnRequestExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.HubResponseExceptionMapper;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.metadata.MetadataCredentialResolverInitializer;
import uk.gov.ida.notification.saml.HubResponseTranslator;
import uk.gov.ida.notification.saml.validation.HubResponseValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.ResponseAttributesValidator;
import uk.gov.ida.notification.resources.HubResponseFromGatewayResource;
import uk.gov.ida.saml.core.validators.DestinationValidator;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.core.validators.assertion.AuthnStatementAssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.DuplicateAssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.IPAddressValidator;
import uk.gov.ida.saml.core.validators.assertion.IdentityProviderAssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.MatchingDatasetAssertionValidator;
import uk.gov.ida.saml.core.validators.subject.AssertionSubjectValidator;
import uk.gov.ida.saml.core.validators.subjectconfirmation.AssertionSubjectConfirmationValidator;
import uk.gov.ida.saml.hub.transformers.inbound.SamlStatusToIdaStatusCodeMapper;
import uk.gov.ida.saml.hub.validators.response.idp.IdpResponseValidator;
import uk.gov.ida.saml.hub.validators.response.idp.components.EncryptedResponseFromIdpValidator;
import uk.gov.ida.saml.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.saml.security.validators.issuer.IssuerValidator;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

        // Verify SAML
        VerifySamlInitializer.init();

        // Views
        bootstrap.addBundle(new ViewBundle<>());

        // Metadata
        hubMetadataResolverBundle = new MetadataResolverBundle<>(TranslatorConfiguration::getHubMetadataConfiguration);
        bootstrap.addBundle(hubMetadataResolverBundle);

        connectorMetadataResolverBundle = new MetadataResolverBundle<>(TranslatorConfiguration::getConnectorMetadataConfiguration);
        bootstrap.addBundle(connectorMetadataResolverBundle);

    }

    @Override
    public void run(final TranslatorConfiguration configuration,
                    final Environment environment) throws ComponentInitializationException {

        connectorMetadata = createMetadata(connectorMetadataResolverBundle);

        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("translator");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);


        registerProviders(environment);
        registerExceptionMappers(environment);
        registerResources(configuration, environment);

    }

    private void registerProviders(Environment environment) {
        environment.jersey().register(AuthnRequestParameterProvider.class);
        environment.jersey().register(ResponseParameterProvider.class);
    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new HubResponseExceptionMapper());
        environment.jersey().register(new AuthnRequestExceptionMapper());
    }

    private void registerResources(TranslatorConfiguration configuration, Environment environment) throws ComponentInitializationException {
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
        SamlObjectSigner signer = new SamlObjectSigner(
                configuration.getConnectorFacingSigningKeyPair().getPublicKey().getPublicKey(),
                configuration.getConnectorFacingSigningKeyPair().getPrivateKey().getPrivateKey(),
                configuration.getConnectorFacingSigningKeyPair().getPublicKey().getCert()
        );
        return new EidasResponseGenerator(hubResponseTranslator, signer);
    }

    private HubResponseValidator createHubResponseValidator(TranslatorConfiguration configuration) throws ComponentInitializationException {
        URI proxyNodeResponseUrl = configuration.getProxyNodeResponseUrl();
        String proxyNodeEntityId = configuration.getProxyNodeEntityId();

        SamlMessageSignatureValidator hubResponseMessageSignatureValidator = createSamlMessagesSignatureValidator(hubMetadataResolverBundle);

        IdpResponseValidator idpResponseValidator = new IdpResponseValidator(
                new SamlResponseSignatureValidator(hubResponseMessageSignatureValidator),
                createDecrypter(configuration.getHubFacingEncryptionKeyPair()),
                new SamlAssertionsSignatureValidator(hubResponseMessageSignatureValidator),
                new EncryptedResponseFromIdpValidator<>(new SamlStatusToIdaStatusCodeMapper()),
                new DestinationValidator(proxyNodeResponseUrl, proxyNodeResponseUrl.getPath()),
                createResponseAssertionsFromIdpValidator(proxyNodeEntityId)
        );
        ResponseAttributesValidator responseAttributesValidator = new ResponseAttributesValidator();
        return new HubResponseValidator(idpResponseValidator, responseAttributesValidator, new LoaValidator());
    }

    private ResponseAssertionsFromIdpValidator createResponseAssertionsFromIdpValidator(String proxyNodeEntityId) {
        IdentityProviderAssertionValidator assertionValidator = new IdentityProviderAssertionValidator(
                new IssuerValidator(),
                new AssertionSubjectValidator(),
                new AssertionAttributeStatementValidator(),
                new AssertionSubjectConfirmationValidator()
        );
        DuplicateAssertionValidator duplicateAssertionValidator = new DuplicateAssertionValidator(new ConcurrentHashMap<>());
        return new ResponseAssertionsFromIdpValidator(
                assertionValidator,
                new MatchingDatasetAssertionValidator(duplicateAssertionValidator),
                new AuthnStatementAssertionValidator(duplicateAssertionValidator),
                new IPAddressValidator(),
                proxyNodeEntityId
        );
    }

    private Metadata createMetadata(MetadataResolverBundle bundle) throws ComponentInitializationException {
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(bundle.getMetadataResolver()).initialize();
        return new Metadata(metadataCredentialResolver);
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

    private SamlMessageSignatureValidator createSamlMessagesSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) throws ComponentInitializationException {
        MetadataCredentialResolver hubMetadataCredentialResolver = new MetadataCredentialResolverInitializer(hubMetadataResolverBundle.getMetadataResolver()).initialize();
        KeyInfoCredentialResolver keyInfoCredentialResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(hubMetadataCredentialResolver, keyInfoCredentialResolver);
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
        return new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
    }

    private SamlRequestSignatureValidator createSamlRequestSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) throws ComponentInitializationException {
        SamlMessageSignatureValidator samlMessageSignatureValidator = createSamlMessagesSignatureValidator(hubMetadataResolverBundle);
        return new SamlRequestSignatureValidator(samlMessageSignatureValidator);
    }
}
