package uk.gov.ida.notification.contracts.verifyserviceprovider;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class AttributesBuilder {

    public static String FIRST_NAME = "Jean";
    public static String MIDDLE_NAME = "Paul";
    public static String LAST_NAME = "Smith";
    public static String GENDER = "NOT_SPECIFIED";
    public static DateTime VALID_FROM = createDateTime(2001, 1, 1, 12, 0);
    public static DateTime DATE_OF_BIRTH = createDateTime(1990, 1, 1, 0, 0);

    private List<Attribute<String>> firstNames = new ArrayList<>(singletonList(createAttribute(FIRST_NAME)));
    private List<Attribute<String>> middleNames = new ArrayList<>(singletonList(createAttribute(MIDDLE_NAME)));
    private List<Attribute<String>> lastNames = new ArrayList<>(singletonList(createAttribute(LAST_NAME)));
    private Attribute<String> gender = createAttribute(GENDER);
    private List<Attribute<DateTime>> datesOfBirth = new ArrayList<>(singletonList(createAttribute(DATE_OF_BIRTH)));

    public Attributes build() {
        return new Attributes(
                firstNames,
                middleNames,
                lastNames,
                datesOfBirth,
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

    public AttributesBuilder addMiddleName(Attribute<String> middleName) {
        this.middleNames.add(middleName);
        return this;
    }

    public AttributesBuilder addLastName(Attribute<String> lastName) {
        this.lastNames.add(lastName);
        return this;
    }

    public AttributesBuilder addDateOfBirth(Attribute<DateTime> dateOfBirth) {
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

    public static DateTime createDateTime(int year, int month, int day, int hour, int minute) {
        return new DateTime(ISOChronology.getInstanceUTC()).withYear(year).withMonthOfYear(month).withDayOfMonth(day)
                .withHourOfDay(hour).withMinuteOfHour(minute);
    }

    public static <T> Attribute<T> createAttribute(T value, boolean verified, DateTime validFrom, DateTime validTo) {
        return new Attribute<>(value, verified, validFrom, validTo);
    }

    private <T> Attribute<T> createAttribute(T value) {
        return new Attribute<>(value, true, VALID_FROM, null);
    }
}
