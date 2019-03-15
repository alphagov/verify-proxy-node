package uk.gov.ida.notification.contracts.verifyserviceprovider;

import org.joda.time.DateTime;

public class TranslatedHubResponseBuilder {

    private String pid = "123456";
    private VspLevelOfAssurance loa = VspLevelOfAssurance.LEVEL_2;
    private VspScenario vspScenario = VspScenario.IDENTITY_VERIFIED;
    private Attributes attributes = new AttributesBuilder().build();

    public TranslatedHubResponseBuilder withScenario(VspScenario scenario) {
        this.vspScenario = scenario;
        return this;
    }

    public TranslatedHubResponseBuilder withAttributes(Attributes attributes) {
        this.attributes = attributes;
        return this;
    }

    public TranslatedHubResponseBuilder withoutAttributes() {
        this.attributes = null;
        return this;
    }

    public TranslatedHubResponseBuilder withLevelOfAssurance(VspLevelOfAssurance loa) {
        this.loa = loa;
        return this;
    }

    public TranslatedHubResponseBuilder withoutPid() {
        this.pid = null;
        return this;
    }

    public TranslatedHubResponse build() {
        return new TranslatedHubResponse(vspScenario, pid, loa, attributes);
    }

    public static TranslatedHubResponse buildTranslatedHubResponseIdentityVerified() {
        return new TranslatedHubResponseBuilder().build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseAuthenticationFailed() {
        return new TranslatedHubResponseBuilder().withScenario(VspScenario.AUTHENTICATION_FAILED).withoutAttributes().build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseRequestError() {
        return new TranslatedHubResponseBuilder().withScenario(VspScenario.REQUEST_ERROR).withoutAttributes().build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseCancellation() {
        return new TranslatedHubResponseBuilder().withScenario(VspScenario.CANCELLATION).withoutAttributes().build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseIdentityVerifiedLOA1() {
        return new TranslatedHubResponseBuilder().withScenario(VspScenario.IDENTITY_VERIFIED).withLevelOfAssurance(VspLevelOfAssurance.LEVEL_1).build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseIdentityVerifiedNoAttributes() {
        return new TranslatedHubResponseBuilder().withScenario(VspScenario.IDENTITY_VERIFIED).withoutAttributes().build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseFirstNameAttributeOnly() {
        return new TranslatedHubResponseBuilder()
                .withScenario(VspScenario.IDENTITY_VERIFIED)
                .withAttributes(
                        new AttributesBuilder()
                                .withoutMiddleName()
                                .withoutLastName()
                                .withoutDateOfBirth()
                                .withoutGender()
                                .build()
                ).build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseAllAttributesMissing() {
        return new TranslatedHubResponseBuilder()
                .withScenario(VspScenario.IDENTITY_VERIFIED)
                .withAttributes(
                        new AttributesBuilder()
                                .withoutFirstName()
                                .withoutMiddleName()
                                .withoutLastName()
                                .withoutDateOfBirth()
                                .withoutGender()
                                .build()
                ).build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseAttributesNullPidNull() {
        return new TranslatedHubResponseBuilder().withoutAttributes().withoutPid().build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseAttributesThreeFirstNamesOnlyLastVerified() {
        return new TranslatedHubResponseBuilder()
                .withScenario(VspScenario.IDENTITY_VERIFIED)
                .withAttributes(
                        new AttributesBuilder()
                                .withoutMiddleName()
                                .withoutLastName()
                                .withoutDateOfBirth()
                                .withoutGender()
                                .withoutFirstName()
                                .addFirstName(AttributesBuilder.createAttribute("FirstNameA", false, createDateTime(2001, 1, 1, 12, 0), null))
                                .addFirstName(AttributesBuilder.createAttribute("FirstNameB", false, createDateTime(2001, 1, 1, 12, 0), null))
                                .addFirstName(AttributesBuilder.createAttribute("FirstNameV", true, createDateTime(2001, 1, 1, 12, 0), null))
                                .build()
                ).build();
    }

    public static TranslatedHubResponse buildTranslatedHubResponseAttributesMultipleValues() {
        return new TranslatedHubResponseBuilder()
                .withScenario(VspScenario.IDENTITY_VERIFIED)
                .withAttributes(
                        new AttributesBuilder()
                                .withoutMiddleName()
                                .withoutLastName()
                                .withoutDateOfBirth()
                                .withoutGender()
                                .withoutFirstName()
                                .addFirstName(AttributesBuilder.createAttribute("FirstNameA", false, createDateTime(2001, 1, 1, 12, 0), null))
                                .addFirstName(AttributesBuilder.createAttribute("FirstNameB", false, createDateTime(2001, 1, 1, 12, 0), null))
                                .addFirstName(AttributesBuilder.createAttribute("FirstNameV", true, createDateTime(2001, 1, 1, 12, 0), null))
                                .addMiddleName(AttributesBuilder.createAttribute("MiddleNameA", true, createDateTime(2001, 1, 1, 12, 0), null))
                                .addMiddleName(AttributesBuilder.createAttribute("MiddleNameB", true, createDateTime(2001, 1, 1, 12, 0), null))
                                .addMiddleName(AttributesBuilder.createAttribute("MiddleNameC", true, createDateTime(2001, 1, 1, 12, 0), null))
                                .addLastName(AttributesBuilder.createAttribute("SurnameA", true, createDateTime(2001, 1, 1, 12, 0), null))
                                .addLastName(AttributesBuilder.createAttribute("SurnameB", true, createDateTime(2001, 1, 1, 12, 0), null))
                                .addLastName(AttributesBuilder.createAttribute("SurnameC", true, createDateTime(2001, 1, 1, 12, 0), null))
                                .addDateOfBirth(AttributesBuilder.createAttribute(createDateTime(1990, 1, 1, 0, 0), false, createDateTime(2001, 1, 1, 12, 0), null))
                                .addDateOfBirth(AttributesBuilder.createAttribute(createDateTime(1985, 9, 7, 14, 0), false, createDateTime(2005, 1, 1, 12, 0), null))
                                .build()
                ).build();
    }

    private static DateTime createDateTime(int year, int month, int day, int hour, int minute) {
        return new DateTime().withYear(year).withMonthOfYear(month).withDayOfMonth(day).withHourOfDay(hour).withMinuteOfHour(minute);
    }
}
