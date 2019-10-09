package uk.gov.ida.notification.eidassaml;

import engineering.reliability.gds.metrics.bundle.PrometheusBundle;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
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
import uk.gov.ida.notification.exceptions.mappers.CatchAllExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.JsonErrorResponseRuntimeExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.JsonErrorResponseValidationExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.SamlTransformationErrorExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.saml.deprecate.DestinationValidator;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.shared.istio.IstioHeaderMapperFilter;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.Optional;

import static uk.gov.ida.notification.saml.SamlSignatureValidatorFactory.createSamlRequestSignatureValidator;

public class EidasSamlApplication extends Application<EidasSamlParserConfiguration> {

    private Metadata connectorMetadata;
    private MetadataResolverBundle<EidasSamlParserConfiguration> connectorMetadataResolverBundle;

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

    public void initialize(final Bootstrap<EidasSamlParserConfiguration> bootstrap) {

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
        bootstrap.addBundle(new PrometheusBundle());
    }

    @Override
    public void run(EidasSamlParserConfiguration configuration, Environment environment) throws Exception {

        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("parser");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);

        connectorMetadata = new Metadata(connectorMetadataResolverBundle.getMetadataCredentialResolver());
        EidasAuthnRequestValidator eidasAuthnRequestValidator = createEidasAuthnRequestValidator(configuration, connectorMetadataResolverBundle);
        SamlRequestSignatureValidator<AuthnRequest> samlRequestSignatureValidator = createSamlRequestSignatureValidator(connectorMetadataResolverBundle);
        String x509EncryptionCert = getX509EncryptionCert(configuration);

        environment.jersey().register(IstioHeaderMapperFilter.class);
        environment.jersey().register(ProxyNodeLoggingFilter.class);

        environment.jersey().register(
                new EidasSamlResource(
                        eidasAuthnRequestValidator,
                        samlRequestSignatureValidator,
                        x509EncryptionCert,
                        Metadata.getAssertionConsumerServiceLocation(
                                configuration.getConnectorMetadataConfiguration().getExpectedEntityId(),
                                connectorMetadataResolverBundle.getMetadataResolver()
                        ))
        );

        registerMetadataHealthCheck(
                connectorMetadataResolverBundle.getMetadataResolver(),
                configuration.getConnectorMetadataConfiguration(),
                environment,
                "connector-metadata");
        registerExceptionMappers(environment);
        registerInjections(environment);
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
        environment.jersey().register(new JsonErrorResponseRuntimeExceptionMapper());
        environment.jersey().register(new JsonErrorResponseValidationExceptionMapper());
        environment.jersey().register(new CatchAllExceptionMapper());
    }

    private EidasAuthnRequestValidator createEidasAuthnRequestValidator(EidasSamlParserConfiguration configuration, MetadataResolverBundle connectorMetadataResolverBundle) throws Exception {
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
                new AssertionConsumerServiceValidator(connectorMetadataResolverBundle.getMetadataResolver())
        );
    }

    private String getX509EncryptionCert(EidasSamlParserConfiguration configuration) throws CertificateEncodingException {
        X509Credential credential = (X509Credential) connectorMetadata.getCredential(UsageType.ENCRYPTION,
                configuration.getConnectorMetadataConfiguration().getExpectedEntityId(),
                SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        return Base64.getEncoder().encodeToString(credential.getEntityCertificate().getEncoded());
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
