package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.exceptions.EidasAuthnRequestException;
import uk.gov.ida.notification.saml.SamlBuilder;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

public class EidasAuthnRequestTranslator {
    private final String proxyNodeEntityId;
    private final String hubUrl;

    public EidasAuthnRequestTranslator(String proxyNodeEntityId, String hubUrl) {
        this.proxyNodeEntityId = proxyNodeEntityId;
        this.hubUrl = hubUrl;
    }

    public AuthnRequest translate(EidasAuthnRequest eidasAuthnRequest) {
        DateTime now = DateTime.now();
        String requestId = eidasAuthnRequest.getRequestId();
        String loa = mapLoa(eidasAuthnRequest.getRequestedLoa());
        return createVerifyAuthnRequest(now, requestId, loa);
    }

    private String mapLoa(String eidasLoa) {
        switch (eidasLoa) {
            case EidasConstants.EIDAS_LOA_SUBSTANTIAL:
                return IdaAuthnContext.LEVEL_2_AUTHN_CTX;
            default:
                throw new EidasAuthnRequestException("Invalid requested level of assurance: " + eidasLoa);
        }
    }

    private AuthnRequest createVerifyAuthnRequest(DateTime issueInstant, String requestId, String loa) {
        RequestedAuthnContext requestedAuthnContext = createRequestedAuthnContext(
                AuthnContextComparisonTypeEnumeration.EXACT,
                loa
        );
        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(requestId);
        authnRequest.setDestination(hubUrl);
        authnRequest.setIssueInstant(issueInstant);
        authnRequest.setIssuer(createIssuer(proxyNodeEntityId));
        authnRequest.setNameIDPolicy(createNameIDPolicy());
        authnRequest.setRequestedAuthnContext(requestedAuthnContext);
        return authnRequest;
    }

    private Issuer createIssuer(String issuerEntityId) {
        Issuer issuer = (Issuer) XMLObjectSupport.buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue(issuerEntityId);
        return issuer;
    }

    private NameIDPolicy createNameIDPolicy() {
        NameIDPolicy nameIDPolicy = (NameIDPolicy) XMLObjectSupport.buildXMLObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setAllowCreate(true);
        nameIDPolicy.setFormat(NameIDType.PERSISTENT);
        return nameIDPolicy;
    }

    private RequestedAuthnContext createRequestedAuthnContext(AuthnContextComparisonTypeEnumeration comparisonType, String loa) {
        RequestedAuthnContext requestedAuthnContext = (RequestedAuthnContext) XMLObjectSupport.buildXMLObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        requestedAuthnContext.setComparison(comparisonType);
        AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) XMLObjectSupport.buildXMLObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(loa);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        return requestedAuthnContext;
    }
}
