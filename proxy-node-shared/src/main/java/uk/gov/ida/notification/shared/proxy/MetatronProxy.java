package uk.gov.ida.notification.shared.proxy;

import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.contracts.metadata.CountryMetadataResponse;
import uk.gov.ida.notification.exceptions.proxy.MetatronResponseException;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MetatronProxy {
    private final ProxyNodeJsonClient metatronClient;
    private final URI metatronUri;
    private static final String METADATA_PATH = "metadata";

    public MetatronProxy(
            URI metatronUri,
            ProxyNodeJsonClient metatronClient) {
        this.metatronClient = metatronClient;
        this.metatronUri = metatronUri;
    }

    public CountryMetadataResponse getCountryMetadata(String entityId) {
        try {
            return metatronClient.get(
                    UriBuilder
                            .fromUri(metatronUri)
                            .path(METADATA_PATH)
                            .path(URLEncoder.encode(entityId, StandardCharsets.UTF_8.toString()))
                            .build(),
                    CountryMetadataResponse.class
            );
        } catch (ApplicationException e) {
            throw new MetatronResponseException(e, entityId);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
