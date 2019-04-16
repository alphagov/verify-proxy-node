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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EidasResponseAttributesHashLoggerTest {

    private static final String PID = "pid1234ABCD";
    private static final Method APPLY_ATTRIBUTES_TO_LOGGER_METHOD = getApplyAttributesToLoggerMethod();

    @Mock
    Attributes attributes;

    @Mock
    EidasResponseAttributesHashLogger attributesHashLogger;

    @Test
    public void shouldOnlyIncludeFirstVerifiedFirstNameInHash() {
        when(attributes.getFirstNames()).thenReturn(List.of(
                new Attribute<>("FirstNameA", false, null, null),
                new Attribute<>("FirstNameV1", true, null, null),
                new Attribute<>("FirstNameB", false, null, null),
                new Attribute<>("FirstNameV2", true, null, null)
        ));

        applyAttributesToLogger();

        verify(attributesHashLogger).setPid(PID);
        verify(attributesHashLogger, times(1)).setFirstName(any());
        verify(attributesHashLogger, never()).setFirstName("FirstNameA");
        verify(attributesHashLogger, never()).setFirstName("FirstNameB");
        verify(attributesHashLogger).setFirstName("FirstNameV1");
        verify(attributesHashLogger, never()).setFirstName("FirstNameV2");

        verify(attributesHashLogger, never()).addMiddleName(any());
        verify(attributesHashLogger, never()).addSurname(any());
        verify(attributesHashLogger, never()).setDateOfBirth(any());
    }

    @Test
    public void shouldIncludeAllMiddleNamesInHash() {
        when(attributes.getMiddleNames()).thenReturn(List.of(
                new Attribute<>("MiddleNameA", false, null, null),
                new Attribute<>("MiddleNameV1", true, null, null),
                new Attribute<>("MiddleNameC", false, null, null),
                new Attribute<>("MiddleNameV2", true, null, null)
        ));

        applyAttributesToLogger();

        verify(attributesHashLogger).setPid(PID);
        verify(attributesHashLogger, times(4)).addMiddleName(any());

        InOrder inOrder = inOrder(attributesHashLogger);
        inOrder.verify(attributesHashLogger).addMiddleName("MiddleNameA");
        inOrder.verify(attributesHashLogger).addMiddleName("MiddleNameV1");
        inOrder.verify(attributesHashLogger).addMiddleName("MiddleNameC");
        inOrder.verify(attributesHashLogger).addMiddleName("MiddleNameV2");

        verify(attributesHashLogger, never()).setFirstName(any());
        verify(attributesHashLogger, never()).addSurname(any());
        verify(attributesHashLogger, never()).setDateOfBirth(any());
    }

    @Test
    public void shouldIncludeAllSurnamesInHash() {
        when(attributes.getSurnames()).thenReturn(List.of(
                new Attribute<>("SurnameV1", true, null, null),
                new Attribute<>("SurnameA", false, null, null),
                new Attribute<>("SurnameV2", true, null, null),
                new Attribute<>("SurnameB", false, null, null)
        ));

        applyAttributesToLogger();

        verify(attributesHashLogger).setPid(PID);
        verify(attributesHashLogger, times(4)).addSurname(any());

        InOrder inOrder = inOrder(attributesHashLogger);
        inOrder.verify(attributesHashLogger).addSurname("SurnameV1");
        inOrder.verify(attributesHashLogger).addSurname("SurnameA");
        inOrder.verify(attributesHashLogger).addSurname("SurnameV2");
        inOrder.verify(attributesHashLogger).addSurname("SurnameB");

        verify(attributesHashLogger, never()).setFirstName(any());
        verify(attributesHashLogger, never()).addMiddleName(any());
        verify(attributesHashLogger, never()).setDateOfBirth(any());
    }

    @Test
    public void shouldOnlyIncludeFirstVerifiedDateOfBirthInHash() {
        final DateTime[] datesOfBirth = new DateTime[]{
                new DateTime(1990, 1, 1, 0, 0),
                new DateTime(1985, 9, 7, 14, 0),
                new DateTime(1984, 10, 1, 0, 0),
                new DateTime(1977, 12, 6, 12, 0)
        };

        when(attributes.getDatesOfBirth()).thenReturn(List.of(
                new Attribute<>(datesOfBirth[0], false, null, null),
                new Attribute<>(datesOfBirth[1], true, null, null),
                new Attribute<>(datesOfBirth[2], false, null, null),
                new Attribute<>(datesOfBirth[3], true, null, null)
        ));

        applyAttributesToLogger();

        verify(attributesHashLogger).setPid(PID);
        verify(attributesHashLogger, times(1)).setDateOfBirth(any());
        verify(attributesHashLogger, never()).setDateOfBirth(datesOfBirth[0]);
        verify(attributesHashLogger).setDateOfBirth(datesOfBirth[1]);
        verify(attributesHashLogger, never()).setDateOfBirth(datesOfBirth[2]);
        verify(attributesHashLogger, never()).setDateOfBirth(datesOfBirth[3]);

        verify(attributesHashLogger, never()).addMiddleName(any());
        verify(attributesHashLogger, never()).addSurname(any());
        verify(attributesHashLogger, never()).setFirstName(any());
    }

    private void applyAttributesToLogger() {
        try {
            APPLY_ATTRIBUTES_TO_LOGGER_METHOD.invoke(null, attributesHashLogger, attributes, PID);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getApplyAttributesToLoggerMethod() {
        Method applyAttributesToLoggerMethod;
        try {
            applyAttributesToLoggerMethod =
                    HubResponseAttributesHashLogger.class.getDeclaredMethod("applyAttributesToHashLogger", EidasResponseAttributesHashLogger.class, Attributes.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        applyAttributesToLoggerMethod.setAccessible(true);

        return applyAttributesToLoggerMethod;
    }
}
