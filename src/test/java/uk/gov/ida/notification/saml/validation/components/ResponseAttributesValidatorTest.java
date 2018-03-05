package uk.gov.ida.notification.saml.validation.components;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.saml.core.test.builders.AddressAttributeValueBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder;
import uk.gov.ida.saml.core.test.builders.DateAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.DateAttributeValueBuilder;
import uk.gov.ida.saml.core.test.builders.GenderAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder;

import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;

public class ResponseAttributesValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static ResponseAttributesValidator responseAttributesValidator;

    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
        VerifySamlInitializer.init();
        responseAttributesValidator = new ResponseAttributesValidator();
    }

    @Test
    public void shouldNotThrowExceptionIfVerifiedAttributesWithMiddleName() throws Throwable {
        AttributeValue firstnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstnameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldNotThrowExceptionIfVerifiedAttributesWithoutMiddleName() throws Throwable {
        AttributeValue firstnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstnameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldNotThrowExceptionIfVerifiedAttributesWithEmptyNotVerifiedMiddleName() throws Throwable {
        AttributeValue firstnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("").build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstnameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfMissingFirstName() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Missing mandatory Response Attribute(s): MDS_firstname");

        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfEmptyFirstName() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Response Attribute needs to be non-empty: MDS_firstname");

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfFirstNameNotVerified() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Response Attribute needs to be verified: MDS_firstname");

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(false).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfMiddleNameNotVerified() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Response Attribute needs to be verified: MDS_middlename");

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(false).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfMissingLastName() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Missing mandatory Response Attribute(s): MDS_surname");

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfEmptyLastName() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Response Attribute needs to be non-empty: MDS_surname");

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfLastNameNotVerified() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Response Attribute needs to be verified: MDS_surname");

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(false).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfMissingDateOfBirth() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Missing mandatory Response Attribute(s): MDS_dateofbirth");

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfEmptyDateOfBirth() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Response Attribute needs to be non-empty: MDS_dateofbirth");

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfDateOfBirthNotVerified() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Response Attribute needs to be verified: MDS_dateofbirth");

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue middleNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("James").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(false).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middleNameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth());

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldNotThrowExceptionIfNonValidatedAttributesAreEmpty() {
        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        Attribute gender = GenderAttributeBuilder_1_1.aGender_1_1().withValue("").withVerified(true).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth())
            .addAttribute(gender);

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldNotThrowExceptionIfNonValidatedAttributesAreNotVerified() {
        AttributeValue firstNameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harry").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Potter").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1980-07-31").withVerified(true).build();

        Attribute gender = GenderAttributeBuilder_1_1.aGender_1_1().withValue("wizard").withVerified(false).build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstNameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth())
            .addAttribute(gender);

        AttributeStatement attributeStatement = attributeStatementBuilder.build();
        responseAttributesValidator.validate(attributeStatement);
    }

    @Test
    public void shouldThrowExceptionIfNullAttributeStatement() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Missing Matching Dataset Attribute Statement");

        responseAttributesValidator.validate(null);
    }
}
