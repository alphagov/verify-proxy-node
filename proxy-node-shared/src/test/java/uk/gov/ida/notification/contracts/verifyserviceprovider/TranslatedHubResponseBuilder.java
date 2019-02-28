package uk.gov.ida.notification.contracts.verifyserviceprovider;

import java.util.Collections;

import org.joda.time.DateTime;

public class TranslatedHubResponseBuilder {

    public static TranslatedHubResponse buildTranslatedHubResponseAuthenticationFailed() {
        return new TranslatedHubResponse(VspScenario.AUTHENTICATION_FAILED, "pid1234", VspLevelOfAssurance.LEVEL_2, null);
    }

    public static TranslatedHubResponse buildTranslatedHubResponseIdentityVerified() {
        return new TranslatedHubResponse(VspScenario.IDENTITY_VERIFIED, "123456", VspLevelOfAssurance.LEVEL_2, buildAttributes());
    }

    public static TranslatedHubResponse buildTranslatedHubResponseIdentityVerifiedLOA1() {
        return new TranslatedHubResponse(VspScenario.IDENTITY_VERIFIED, "123456", VspLevelOfAssurance.LEVEL_1, buildAttributes());
    }

    public static TranslatedHubResponse buildTranslatedHubResponseIdentityVerifiedNoAttributes() {
        return new TranslatedHubResponse(VspScenario.IDENTITY_VERIFIED, "123456", VspLevelOfAssurance.LEVEL_2, null);
    }

    public static TranslatedHubResponse buildTranslatedHubResponseRequestError() {
        return new TranslatedHubResponse(VspScenario.REQUEST_ERROR, "123456", VspLevelOfAssurance.LEVEL_2, null);
    }

    public static TranslatedHubResponse buildTranslatedHubResponseIncompleteAttributes() {
        return new TranslatedHubResponse(VspScenario.IDENTITY_VERIFIED, "123456", VspLevelOfAssurance.LEVEL_2, buildAttributesOneAttributeOnly());
    }

    public static TranslatedHubResponse buildTranslatedHubResponseCancellation() {
        return new TranslatedHubResponse(VspScenario.CANCELLATION, "pid1234", VspLevelOfAssurance.LEVEL_2, null);
    }

    private static Attributes buildAttributes() {
        return new Attributes(
                new Attribute<>("Jean Paul", true, createDateTime(2001, 1, 1, 12, 0), null),
                null,
                Collections.singletonList(new Attribute<>("Smith", true, createDateTime(2001, 1, 1, 12, 0), null)),
                new Attribute<>(createDateTime(1990, 1, 1, 0, 0), true, createDateTime(2001, 1, 1, 12, 0), null),
                new Attribute<>("NOT_SPECIFIED", true, createDateTime(2001, 1, 1, 12, 0), null),
                Collections.singletonList(new Attribute<>(new Address(Collections.singletonList("1 Acacia Avenue"), "SW1A 1AA", null, null),
                        true, createDateTime(2001, 1, 1, 12, 0), null)));
    }

    private static Attributes buildAttributesOneAttributeOnly() {
        return new Attributes(
                new Attribute<>("Jean Paul", true, createDateTime(2001, 1, 1, 12, 0), null),
                null,
                null,
                null,
                null,
                null
        );
    }

    private static DateTime createDateTime(int year, int month, int day, int hour, int minute) {
        return new DateTime().withYear(year).withMonthOfYear(month).withDayOfMonth(day).withHourOfDay(hour).withMinuteOfHour(minute);
    }
}