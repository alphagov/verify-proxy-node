package uk.gov.ida.notification.views;

import io.dropwizard.views.View;

import java.net.URL;
import java.util.List;

public class MetadataSigningCertsView extends View {

    private final URL metadataUrl;
    private final List<Cert> certs;

    public MetadataSigningCertsView(URL metadataUrl, List<Cert> certs) {
        super("metadata-signing-certs.mustache");

        this.metadataUrl = metadataUrl;
        this.certs = certs;
    }

    public URL getMetadataUrl() {
        return metadataUrl;
    }

    public List<Cert> getCerts() {
        return certs;
    }

    public static class Cert {
        private final String subjectCommonName;
        private final String issuerCommonName;
        private final String validNotBefore;
        private final String validNotAfter;
        private final String certPem;

        public Cert(String certPem, String subjectCommonName, String issuerCommonName, String validNotBefore, String validNotAfter) {
            this.subjectCommonName = subjectCommonName;
            this.issuerCommonName = issuerCommonName;
            this.validNotBefore = validNotBefore;
            this.validNotAfter = validNotAfter;
            this.certPem = certPem;
        }

        public String getSubjectCommonName() {
            return subjectCommonName;
        }

        public String getIssuerCommonName() {
            return issuerCommonName;
        }

        public String getValidNotBefore() {
            return validNotBefore;
        }

        public String getValidNotAfter() {
            return validNotAfter;
        }

        public String getCertPem() {
            return certPem;
        }
    }
}
