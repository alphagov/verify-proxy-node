package uk.gov.ida.notification.shared.metadata;

import uk.gov.ida.notification.exceptions.metadata.InvalidMetadataException;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.views.MetadataSigningCertsView;
import uk.gov.ida.notification.views.MetadataSigningCertsView.Cert;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

public class MetadataCertsPublishingResource {

    private static final String BEGIN_LINE = "-----BEGIN CERTIFICATE-----";
    private static final String END_LINE = "-----END CERTIFICATE-----";
    private static final String COMMON_NAME_PREFIX = "CN=";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    private static MetadataSigningCertsView metadataSigningCertsView;

    @Context
    private UriInfo uriInfo;

    private URI metadataPublishPath;
    private URI metadataCACertsFilePath;
    private URI metadataSigningCertFilePath;

    @Inject
    public MetadataCertsPublishingResource(
            @Named("metadataSigningCertFilePath") URI metadataSigningCertFilePath,
            @Named("metadataCACertsFilePath") URI metadataCACertsFilePath,
            @Named("metadataPublishPath") URI metadataPublishPath) {
        this.metadataSigningCertFilePath = metadataSigningCertFilePath;
        this.metadataCACertsFilePath = metadataCACertsFilePath;
        this.metadataPublishPath = metadataPublishPath;
    }

    @GET
    public MetadataSigningCertsView getMetadataSigningCerts() {
        if (metadataSigningCertsView == null) {
            metadataSigningCertsView = generateMetadataSigningCertsView();
        }

        return metadataSigningCertsView;
    }

    private MetadataSigningCertsView generateMetadataSigningCertsView() {
        final URL metadataPublishUrl;
        try {
            metadataPublishUrl = new URL(this.uriInfo.getBaseUri().toURL(), this.metadataPublishPath.toString());
        } catch (MalformedURLException e) {
            throw new InvalidMetadataException(format(
                    "Couldn't construct metadata publish path from base URI {0} and Path {1}",
                    uriInfo.getBaseUri(), metadataPublishPath), e);
        }

        final String concatenatedPemCerts = readFile(metadataCACertsFilePath).orElseThrow(() ->
                new InvalidMetadataException("Couldn't read metadata certs file from " + metadataCACertsFilePath));

        final List<Cert> certificates = Arrays
                .stream(concatenatedPemCerts.split(BEGIN_LINE))
                .filter(s -> !s.isBlank())
                .map(c -> c.substring(0, c.indexOf(END_LINE)))
                .map(c -> BEGIN_LINE + c + END_LINE)
                .map(this::createCert)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        readFile(metadataSigningCertFilePath).flatMap(this::createCert).ifPresentOrElse(
                msc -> certificates.removeIf(
                        c -> c.getSubjectCommonName().equals(msc.getSubjectCommonName()) &&
                                c.getIssuerCommonName().equals(msc.getIssuerCommonName())),
                () -> ProxyNodeLogger.warning("Couldn't read metadata signing cert file from " + metadataSigningCertFilePath));

        if (certificates.isEmpty()) {
            throw new InvalidMetadataException("No valid metadata signing certs extracted from \n" + concatenatedPemCerts);
        }

        return new MetadataSigningCertsView(metadataPublishUrl, certificates);
    }

    private Optional<Cert> createCert(String certPem) {
        return Optional.ofNullable(generateCertificate(certPem))
                .map(c -> new Cert(certPem, getCommonName(c.getSubjectDN()), getCommonName(c.getIssuerDN()),
                        DATE_FORMAT.format(c.getNotBefore()), DATE_FORMAT.format(c.getNotAfter())));
    }

    private String getCommonName(Principal principal) {
        return Arrays.stream(principal.getName().split(","))
                .filter(s -> s.startsWith(COMMON_NAME_PREFIX))
                .findFirst()
                .map(s -> s.replace(COMMON_NAME_PREFIX, ""))
                .map(String::trim)
                .get();
    }

    private X509Certificate generateCertificate(String certPem) {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(certPem.getBytes())) {
            return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(inputStream);
        } catch (CertificateException | IOException e) {
            ProxyNodeLogger.logException(e, "Could not create X509 certificate from PEM string: \n" + certPem);
            return null;
        }
    }

    private Optional<String> readFile(URI filePath) {
        final File file = new File(filePath.toString());
        if (!file.exists()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Files.readString(file.toPath()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
