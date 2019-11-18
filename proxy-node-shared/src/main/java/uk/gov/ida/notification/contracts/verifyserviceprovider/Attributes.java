package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAddress;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingTransliterableAttribute;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingVerifiableAttribute;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Attributes extends NonMatchingAttributes {
    public Attributes(
            List<NonMatchingTransliterableAttribute> firstNames,
            List<NonMatchingVerifiableAttribute<String>> middleNames,
            List<NonMatchingTransliterableAttribute> surnames,
            List<NonMatchingVerifiableAttribute<LocalDate>> datesOfBirth,
            NonMatchingVerifiableAttribute<Gender> gender,
            List<NonMatchingVerifiableAttribute<NonMatchingAddress>> addresses) {
        super(firstNames, middleNames, surnames, datesOfBirth, gender, addresses);
    }

    public static Attributes fromNonMatchingAttributes(NonMatchingAttributes nonMatchingAttributes) {

        return new Attributes(
                nonMatchingAttributes.getFirstNames(),
                nonMatchingAttributes.getMiddleNames(),
                nonMatchingAttributes.getSurnames(),
                nonMatchingAttributes.getDatesOfBirth(),
                nonMatchingAttributes.getGender(),
                nonMatchingAttributes.getAddresses()
        );
    }

    @JsonIgnore
    public AttributesList<String> getFirstNamesAttributesList() {
        return new AttributesList(firstNames, "firstName");
    }

    @JsonIgnore
    public AttributesList<String> getMiddleNamesAttributesList() {
        return new AttributesList(middleNames, "middleName");
    }

    @JsonIgnore
    public AttributesList<String> getSurnamesAttributesList() {
        return new AttributesList(surnames, "surname");
    }

    @JsonIgnore
    public AttributesList<LocalDate> getDatesOfBirthAttributesList() {
        return new AttributesList(datesOfBirth, "dateOfBirth");
    }

    @JsonIgnore
    public NonMatchingVerifiableAttribute<Gender> getGender() {
        return gender;
    }

    @JsonIgnore
    public AttributesList<Address> getAddressesAttributesList() {
        return new AttributesList(addresses, "address");
    }

    public class AttributesList<T> {

        private final List<NonMatchingVerifiableAttribute<T>> attributes;
        private final String type;

        AttributesList(List<NonMatchingVerifiableAttribute<T>> attributes, String type) {
            this.attributes = Optional.ofNullable(attributes).orElse(Collections.emptyList());
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public List<NonMatchingVerifiableAttribute<T>> getValidAttributes() {

            var current = getAllAttributes().stream()
                    .filter(NonMatchingVerifiableAttribute::isCurrent)
                    .collect(Collectors.toList());

            var verifiedAndCurrent = current.stream()
                    .filter(NonMatchingVerifiableAttribute::isVerified)
                    .collect(Collectors.toList());

            if (!verifiedAndCurrent.isEmpty()) {
                return verifiedAndCurrent;
            }

            ProxyNodeLogger.info("No verified and current attributes: " + createAttributesMessage());
            return current;
        }

        public List<NonMatchingVerifiableAttribute<T>> getAllAttributes() {
            return Collections.unmodifiableList(attributes);
        }

        public String createAttributesMessage() {
            return "[ " + type + " " + attributes.stream()
                    .map(NonMatchingVerifiableAttribute::toString)
                    .collect(Collectors.joining(", ")) + " ]";
        }

    }
}
