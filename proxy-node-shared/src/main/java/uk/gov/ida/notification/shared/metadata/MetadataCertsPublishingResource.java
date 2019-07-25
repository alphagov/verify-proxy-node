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
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

public class MetadataCertsPublishingResource {

    private static final String BEGIN_LINE = "-----BEGIN CERTIFICATE-----";
    private static final String END_LINE = "-----END CERTIFICATE-----";
    private static final String COMMON_NAME_PREFIX = "CN=";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    @Context
    private UriInfo uriInfo;

    private URI metadataPublishPath;
    private URI metadataCACertsFilePath;
    private String metadataSigningCertBase64;

    @Inject
    public MetadataCertsPublishingResource(
            @Named("metadataSigningCertBase64") String metadataSigningCertBase64,
            @Named("metadataCACertsFilePath") URI metadataCACertsFilePath,
            @Named("metadataPublishPath") URI metadataPublishPath) {
        this.metadataSigningCertBase64 = metadataSigningCertBase64;
        this.metadataCACertsFilePath = metadataCACertsFilePath;
        this.metadataPublishPath = metadataPublishPath;
    }

    @GET
    public MetadataSigningCertsView getMetadataSigningCerts() {

        final URL metadataPublishUrl;
        try {
            metadataPublishUrl = new URL(this.uriInfo.getBaseUri().toURL(), this.metadataPublishPath.toString());
        } catch (MalformedURLException e) {
            throw new InvalidMetadataException(format(
                    "Couldn't construct metadata publish path from base URI {0} and Path {1}",
                    uriInfo.getBaseUri(), metadataPublishPath), e);
        }

        final File concatenatedPemCertsFile = new File(metadataCACertsFilePath.toString());
        if (!concatenatedPemCertsFile.exists()) {
            throw new InvalidMetadataException(format(
                    "No file containing metadata signing certs found at {0}", metadataCACertsFilePath));
        }

        String concatenatedPemCerts;
        try {
            concatenatedPemCerts = Files.readString(concatenatedPemCertsFile.toPath());
        } catch (IOException e) {
            throw new InvalidMetadataException(format(
                    "Couldn't read metadata certs file from {0}", metadataCACertsFilePath), e);
        }

        final List<Cert> certificates = Arrays
                .stream(concatenatedPemCerts.split(BEGIN_LINE))
                .filter(s -> !s.isBlank())
                .map(c -> c.substring(0, c.indexOf(END_LINE)))
                .map(c -> c.replace("\n", ""))
                .filter(c -> !c.equals(metadataSigningCertBase64.replace("\n", "").trim()))
                .map(String::trim)
                .map(this::createCert)
                .collect(Collectors.toList());

        if (certificates.isEmpty()) {
            throw new InvalidMetadataException(format(
                    "No valid metadata signing certs extracted from \n{0}", concatenatedPemCerts));
        }

        return new MetadataSigningCertsView(metadataPublishUrl, certificates);
    }

    private Cert createCert(String certBase64) {
        return Optional.ofNullable(generateCertificate(certBase64))
                .map(c -> new Cert(certBase64, getSubjectCommonName(c), getIssuerCommonName(c),
                        DATE_FORMAT.format(c.getNotBefore()), DATE_FORMAT.format(c.getNotAfter())))
                .orElse(null);
    }

    private String getSubjectCommonName(X509Certificate cert) {
        return Optional.of(cert).map(c -> getCommonName(c.getSubjectDN())).orElse("");
    }

    private String getIssuerCommonName(X509Certificate cert) {
        return Optional.of(cert).map(c -> getCommonName(c.getIssuerDN())).orElse("");
    }

    private String getCommonName(Principal principal) {
        return Arrays.stream(principal.getName().split(","))
                .filter(s -> s.startsWith(COMMON_NAME_PREFIX))
                .findFirst()
                .map(s -> s.replace(COMMON_NAME_PREFIX, ""))
                .map(String::trim)
                .orElse("");
    }

    private X509Certificate generateCertificate(String certBase64) {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certBase64));

        try {
            return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(inputStream);
        } catch (CertificateException e) {
            ProxyNodeLogger.logException(e,
                    format("Could not create X509 certificate from base64 encoded string: {0}", certBase64));
            return null;
        }
    }
}
