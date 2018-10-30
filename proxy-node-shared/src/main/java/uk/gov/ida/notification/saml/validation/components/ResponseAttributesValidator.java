package uk.gov.ida.notification.saml.validation.components;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.CollectionUtils;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.StringBasedMdsAttributeValue;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public class ResponseAttributesValidator {
    private static final ImmutableSet<String> mandatoryAttributes = ImmutableSet.of(
        IdaConstants.Attributes_1_1.Firstname.NAME,
        IdaConstants.Attributes_1_1.Surname.NAME,
        IdaConstants.Attributes_1_1.DateOfBirth.NAME
    );

    private static final Set<String> validatedAttributes = concat(
        mandatoryAttributes.stream(),
        Stream.of(IdaConstants.Attributes_1_1.Middlename.NAME)
    ).collect(Collectors.toSet());

    public void validate(AttributeStatement attributeStatement) {
        if (attributeStatement == null)
            throw new InvalidHubResponseException("Missing Matching Dataset Attribute Statement");

        List<String> attributeNames = attributeStatement.getAttributes().stream()
            .filter(attribute -> validatedAttributes.contains(attribute.getName()))
            .map(this::validateAttribute)
            .map(Attribute::getName)
            .collect(Collectors.toList());

        Collection missingMandatoryAttributes = CollectionUtils.subtract(mandatoryAttributes, attributeNames);
        if (!missingMandatoryAttributes.isEmpty())
            throw new InvalidHubResponseException(MessageFormat.format("Missing mandatory Response Attribute(s): {0}", String.join(", ", missingMandatoryAttributes)));
    }

    private Attribute validateAttribute(Attribute attribute) throws InvalidHubResponseException {
        StringBasedMdsAttributeValue attributeValue = (StringBasedMdsAttributeValue) attribute.getAttributeValues().get(0);

        if (Strings.isNullOrEmpty(attributeValue.getValue())) {
            if (isMiddleName(attribute)) return attribute;
            throw new InvalidHubResponseException(MessageFormat.format("Response Attribute needs to be non-empty: {0}", attribute.getName()));
        }

        if (!attributeValue.getVerified())
            throw new InvalidHubResponseException(MessageFormat.format("Response Attribute needs to be verified: {0}", attribute.getName()));

        return attribute;
    }

    private boolean isMiddleName(Attribute attribute) {
        return IdaConstants.Attributes_1_1.Middlename.NAME.equals(attribute.getName());
    }
}
