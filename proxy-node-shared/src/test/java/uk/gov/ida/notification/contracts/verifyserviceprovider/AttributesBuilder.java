package uk.gov.ida.notification.contracts.verifyserviceprovider;

import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.NonMatchingAddress;
import uk.gov.ida.saml.core.domain.NonMatchingAttributes;
import uk.gov.ida.saml.core.domain.NonMatchingTransliterableAttribute;
import uk.gov.ida.saml.core.domain.NonMatchingVerifiableAttribute;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.ida.saml.core.domain.Gender.NOT_SPECIFIED;

public class AttributesBuilder {

    public static final String FIRST_NAME = "Jean";
    public static final String MIDDLE_NAME = "Paul";
    public static final String LAST_NAME = "Smith";
    public static final LocalDate VALID_FROM = createDateTime(2001, 1, 1);
    public static final LocalDate DATE_OF_BIRTH = createDateTime(1990, 1, 1);

    private List<NonMatchingTransliterableAttribute> firstNames = new ArrayList<>(singletonList(createNonMatchingTransliterableAttribute(FIRST_NAME)));
    private List<NonMatchingVerifiableAttribute<String>> middleNames = new ArrayList<>(singletonList(createNonMatchingVerifiableAttribute(MIDDLE_NAME)));
    private List<NonMatchingTransliterableAttribute> lastNames = new ArrayList<>(singletonList(createNonMatchingTransliterableAttribute(LAST_NAME)));
    private NonMatchingVerifiableAttribute<Gender> gender = createNonMatchingVerifiableAttribute(NOT_SPECIFIED);
    private List<NonMatchingVerifiableAttribute<LocalDate>> datesOfBirth = new ArrayList<>(singletonList(createNonMatchingVerifiableAttribute(DATE_OF_BIRTH)));

    public NonMatchingAttributes build() {
        return new NonMatchingAttributes(
                firstNames,
                middleNames,
                lastNames,
                datesOfBirth,
                gender,
                singletonList(createNonMatchingVerifiableAttribute(new NonMatchingAddress(singletonList("1 Acacia Avenue"), "SW1A 1AA", null, null))));
    }

    public AttributesBuilder addFirstName(NonMatchingTransliterableAttribute firstName) {
        this.firstNames.add(firstName);
        return this;
    }

    public AttributesBuilder withFirstName(NonMatchingTransliterableAttribute firstName) {
        this.firstNames = singletonList(firstName);
        return this;
    }

    public AttributesBuilder addMiddleName(NonMatchingVerifiableAttribute<String> middleName) {
        this.middleNames.add(middleName);
        return this;
    }

    public AttributesBuilder addLastName(NonMatchingTransliterableAttribute lastName) {
        this.lastNames.add(lastName);
        return this;
    }

    public AttributesBuilder addDateOfBirth(NonMatchingVerifiableAttribute<LocalDate> dateOfBirth) {
        this.datesOfBirth.add(dateOfBirth);
        return this;
    }

    public AttributesBuilder withoutFirstName() {
        this.firstNames = new ArrayList<>();
        return this;
    }

    public AttributesBuilder withoutMiddleName() {
        this.middleNames = new ArrayList<>();
        return this;
    }

    public AttributesBuilder withoutLastName() {
        this.lastNames = new ArrayList<>();
        return this;
    }

    public AttributesBuilder withoutDateOfBirth() {
        this.datesOfBirth = new ArrayList<>();
        return this;
    }

    public AttributesBuilder withoutGender() {
        this.gender = null;
        return this;
    }

    public static LocalDate createDateTime(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    public static <T> NonMatchingVerifiableAttribute<T> createAttribute(T value, boolean verified, LocalDate validFrom, LocalDate validTo) {
        return new NonMatchingVerifiableAttribute<>(value, verified, validFrom, validTo);
    }

    public static NonMatchingTransliterableAttribute createNonMatchingTransliterableAttribute(
            String value,
            boolean verified,
            LocalDate validFrom,
            LocalDate validTo) {
        return new NonMatchingTransliterableAttribute(value, value, verified, validFrom, validTo);
    }

    public static <T> NonMatchingVerifiableAttribute<T> createNonMatchingVerifiableAttribute(
            T value,
            boolean verified,
            LocalDate validFrom,
            LocalDate validTo) {
        return new NonMatchingVerifiableAttribute<T>(value, verified, validFrom, validTo);
    }

    private NonMatchingTransliterableAttribute createNonMatchingTransliterableAttribute(String value) {
        return new NonMatchingTransliterableAttribute(value, value, true, VALID_FROM, null);
    }

    private <T> NonMatchingVerifiableAttribute<T> createNonMatchingVerifiableAttribute(T value) {
        return new NonMatchingVerifiableAttribute<T>(value, true, VALID_FROM, null);
    }
}
