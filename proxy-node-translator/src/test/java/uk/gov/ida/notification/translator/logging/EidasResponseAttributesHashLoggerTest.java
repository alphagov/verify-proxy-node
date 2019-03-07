package uk.gov.ida.notification.translator.logging;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.saml.core.transformers.EidasResponseAttributesHashLogger;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EidasResponseAttributesHashLoggerTest {

    private final static String PID = "pid1234ABCD";

    @Mock
    Attributes attributesMock;

    @Mock
    EidasResponseAttributesHashLogger hashLoggerMock;

    @Test
    public void shouldOnlyIncludeFirstVerifiedFirstNameInHash() {

        when(attributesMock.getFirstNames()).thenReturn(List.of(
                new Attribute<>("FirstNameA", false, null, null),
                new Attribute<>("FirstNameV1", true, null, null),
                new Attribute<>("FirstNameB", false, null, null),
                new Attribute<>("FirstNameV2", true, null, null)
        ));

        final EidasResponseAttributesHashLoggerHelper eidasResponseAttributesHashLoggerHelper =
                new EidasResponseAttributesHashLoggerHelper(hashLoggerMock);

        eidasResponseAttributesHashLoggerHelper.applyAttributesToHashLogger(
                attributesMock,
                PID
        );

        verify(hashLoggerMock).setPid(PID);
        verify(hashLoggerMock, times(1)).setFirstName(any());
        verify(hashLoggerMock, never()).setFirstName("FirstNameA");
        verify(hashLoggerMock, never()).setFirstName("FirstNameB");
        verify(hashLoggerMock).setFirstName("FirstNameV1");
        verify(hashLoggerMock, never()).setFirstName("FirstNameV2");

        verify(hashLoggerMock, never()).addMiddleName(any());
        verify(hashLoggerMock, never()).addSurname(any());
        verify(hashLoggerMock, never()).setDateOfBirth(any());
    }

    @Test
    public void shouldIncludeAllMiddleNamesInHash() {

        when(attributesMock.getMiddleNames()).thenReturn(List.of(
                new Attribute<>("MiddleNameA", false, null, null),
                new Attribute<>("MiddleNameV1", true, null, null),
                new Attribute<>("MiddleNameC", false, null, null),
                new Attribute<>("MiddleNameV2", true, null, null)
        ));

        final EidasResponseAttributesHashLoggerHelper eidasResponseAttributesHashLoggerHelper =
                new EidasResponseAttributesHashLoggerHelper(hashLoggerMock);

        eidasResponseAttributesHashLoggerHelper.applyAttributesToHashLogger(
                attributesMock,
                PID
        );

        verify(hashLoggerMock).setPid(PID);
        verify(hashLoggerMock, times(4)).addMiddleName(any());

        InOrder inOrder = inOrder(hashLoggerMock);
        inOrder.verify(hashLoggerMock).addMiddleName("MiddleNameA");
        inOrder.verify(hashLoggerMock).addMiddleName("MiddleNameV1");
        inOrder.verify(hashLoggerMock).addMiddleName("MiddleNameC");
        inOrder.verify(hashLoggerMock).addMiddleName("MiddleNameV2");

        verify(hashLoggerMock, never()).setFirstName(any());
        verify(hashLoggerMock, never()).addSurname(any());
        verify(hashLoggerMock, never()).setDateOfBirth(any());
    }

    @Test
    public void shouldIncludeAllSurnamesInHash() {

        when(attributesMock.getSurnames()).thenReturn(List.of(
                new Attribute<>("SurnameV1", true, null, null),
                new Attribute<>("SurnameA", false, null, null),
                new Attribute<>("SurnameV2", true, null, null),
                new Attribute<>("SurnameB", false, null, null)
        ));

        final EidasResponseAttributesHashLoggerHelper eidasResponseAttributesHashLoggerHelper =
                new EidasResponseAttributesHashLoggerHelper(hashLoggerMock);

        eidasResponseAttributesHashLoggerHelper.applyAttributesToHashLogger(
                attributesMock,
                PID
        );

        verify(hashLoggerMock).setPid(PID);
        verify(hashLoggerMock, times(4)).addSurname(any());

        InOrder inOrder = inOrder(hashLoggerMock);
        inOrder.verify(hashLoggerMock).addSurname("SurnameV1");
        inOrder.verify(hashLoggerMock).addSurname("SurnameA");
        inOrder.verify(hashLoggerMock).addSurname("SurnameV2");
        inOrder.verify(hashLoggerMock).addSurname("SurnameB");

        verify(hashLoggerMock, never()).setFirstName(any());
        verify(hashLoggerMock, never()).addMiddleName(any());
        verify(hashLoggerMock, never()).setDateOfBirth(any());
    }

    @Test
    public void shouldOnlyIncludeFirstVerifiedDateOfBirthInHash() {

        DateTime[] datesOfBirth = new DateTime[]{
                new DateTime(1990, 1, 1, 0, 0),
                new DateTime(1985, 9, 7, 14, 0),
                new DateTime(1984, 10, 1, 0, 0),
                new DateTime(1977, 12, 6, 12, 0)
        };

        when(attributesMock.getDatesOfBirth()).thenReturn(List.of(
                new Attribute<>(datesOfBirth[0], false, null, null),
                new Attribute<>(datesOfBirth[1], true, null, null),
                new Attribute<>(datesOfBirth[2], false, null, null),
                new Attribute<>(datesOfBirth[3], true, null, null)
        ));

        final EidasResponseAttributesHashLoggerHelper eidasResponseAttributesHashLoggerHelper =
                new EidasResponseAttributesHashLoggerHelper(hashLoggerMock);

        eidasResponseAttributesHashLoggerHelper.applyAttributesToHashLogger(
                attributesMock,
                PID
        );

        verify(hashLoggerMock).setPid(PID);
        verify(hashLoggerMock, times(1)).setDateOfBirth(any());
        verify(hashLoggerMock, never()).setDateOfBirth(datesOfBirth[0]);
        verify(hashLoggerMock).setDateOfBirth(datesOfBirth[1]);
        verify(hashLoggerMock, never()).setDateOfBirth(datesOfBirth[2]);
        verify(hashLoggerMock, never()).setDateOfBirth(datesOfBirth[3]);

        verify(hashLoggerMock, never()).addMiddleName(any());
        verify(hashLoggerMock, never()).addSurname(any());
        verify(hashLoggerMock, never()).setFirstName(any());
    }
}
