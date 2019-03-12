package uk.gov.ida.notification.contracts.verifyserviceprovider;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class AttributesBuilder {

    public static String FIRST_NAME = "Jean";
    public static String MIDDLE_NAME = "Paul";
    public static String LAST_NAME = "Smith";
    public static String GENDER = "NOT_SPECIFIED";
    public static DateTime VALID_FROM = createDateTime(2001, 1, 1, 12, 0);
    public static DateTime DATE_OF_BIRTH = createDateTime(1990, 1, 1, 0, 0);

    private List<Attribute<String>> firstNames = new ArrayList<>(singletonList(createAttribute(FIRST_NAME)));
    private Attribute<String> middleName = createAttribute(MIDDLE_NAME);
    private Attribute<String> lastName = createAttribute(LAST_NAME);
    private Attribute<String> gender = createAttribute(GENDER);
    private Attribute<DateTime> dateOfBirth = createAttribute(DATE_OF_BIRTH);

    public Attributes build() {
        return new Attributes(
                firstNames,
                middleName != null ? singletonList(middleName) : emptyList(),
                lastName != null ? singletonList(lastName) : emptyList(),
                dateOfBirth != null ? singletonList(dateOfBirth) : emptyList(),
                gender,
                singletonList(createAttribute(new Address(singletonList("1 Acacia Avenue"), "SW1A 1AA", null, null))));
    }

    public AttributesBuilder addFirstName(Attribute<String> firstName) {
        this.firstNames.add(firstName);
        return this;
    }

    public AttributesBuilder withFirstName(Attribute<String> firstName) {
        this.firstNames = singletonList(firstName);
        return this;
    }

    public AttributesBuilder withoutFirstName() {
        this.firstNames = emptyList();
        return this;
    }

    public AttributesBuilder withoutMiddleName() {
        this.middleName = null;
        return this;
    }

    public AttributesBuilder withoutLastName() {
        this.lastName = null;
        return this;
    }

    public AttributesBuilder withoutDateOfBirth() {
        this.dateOfBirth = null;
        return this;
    }

    public AttributesBuilder withoutGender() {
        this.gender = null;
        return this;
    }

    public static DateTime createDateTime(int year, int month, int day, int hour, int minute) {
        return new DateTime().withYear(year).withMonthOfYear(month).withDayOfMonth(day).withHourOfDay(hour).withMinuteOfHour(minute);
    }

    public static <T> Attribute<T> createAttribute(T value, boolean verified, DateTime validFrom, DateTime validTo) {
        return new Attribute<>(value, verified, validFrom, validTo);
    }

    private <T> Attribute<T> createAttribute(T value) {
        return new Attribute<>(value, true, VALID_FROM, null);
    }
}
