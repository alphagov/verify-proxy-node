package uk.gov.ida.notification.eidassaml;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.X509Credential;
import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.eidassaml.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.AssertionConsumerServiceValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.ComparisonValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.RequestedAttributesValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.SpTypeValidator;
import uk.gov.ida.notification.exceptions.mappers.InvalidAuthnRequestExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.SamlTransformationErrorExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.saml.deprecate.DestinationValidator;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.shared.IstioHeaderMapperFilter;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.Optional;

import static uk.gov.ida.notification.saml.SamlSignatureValidatorFactory.createSamlRequestSignatureValidator;

public class EidasSamlApplication extends Application<EidasSamlConfiguration> {

    private Metadata connectorMetadata;
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

        connectorMetadataResolverBundle = new MetadataResolverBundle<>(configuration -> Optional.of(configuration.getConnectorMetadataConfiguration()));

        bootstrap.addBundle(connectorMetadataResolverBundle);
        bootstrap.addBundle(new LogstashBundle());
    }

    @Override
    public void run(EidasSamlConfiguration configuration, Environment environment) throws Exception {

        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("parser");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);

        connectorMetadata = new Metadata(connectorMetadataResolverBundle.getMetadataCredentialResolver());
        EidasAuthnRequestValidator eidasAuthnRequestValidator = createEidasAuthnRequestValidator(configuration, connectorMetadataResolverBundle);
        SamlRequestSignatureValidator samlRequestSignatureValidator = createSamlRequestSignatureValidator(connectorMetadataResolverBundle);
        String x509EncryptionCert = getX509EncryptionCert(configuration);

        environment.jersey().register(IstioHeaderMapperFilter.class);

        environment.jersey().register(
                new EidasSamlResource(
                        eidasAuthnRequestValidator,
                        samlRequestSignatureValidator,
                        x509EncryptionCert,
                        Metadata.getAssertionConsumerServiceLocation(
                                configuration.getConnectorMetadataConfiguration().getExpectedEntityId(),
                                connectorMetadataResolverBundle.getMetadataResolver()
                        )
                )
        );

        registerMetadataHealthCheck(
                connectorMetadataResolverBundle.getMetadataResolver(),
                configuration.getConnectorMetadataConfiguration(),
                environment,
                "connector-metadata");
        registerExceptionMappers(environment);
    }

    private void registerMetadataHealthCheck(MetadataResolver metadataResolver, MetadataConfiguration connectorMetadataConfiguration, Environment environment, String name) {
        MetadataHealthCheck metadataHealthCheck = new MetadataHealthCheck(
                metadataResolver,
                name,
                connectorMetadataConfiguration.getExpectedEntityId()
        );

        environment.healthChecks().register(metadataHealthCheck.getName(), metadataHealthCheck);
    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new SamlTransformationErrorExceptionMapper());
        environment.jersey().register(new InvalidAuthnRequestExceptionMapper());
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

    private String getX509EncryptionCert(EidasSamlConfiguration configuration) throws CertificateEncodingException {

        X509Credential credential = (X509Credential) connectorMetadata.getCredential(UsageType.ENCRYPTION,
                configuration.getConnectorMetadataConfiguration().getExpectedEntityId(),
                SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        String x509EncryptionCert = Base64.getEncoder().encodeToString(
                credential.getEntityCertificate().getEncoded());

        return x509EncryptionCert;
    }
}
