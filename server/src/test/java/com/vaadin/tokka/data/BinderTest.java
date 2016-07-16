package com.vaadin.tokka.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.UserError;
import com.vaadin.tests.data.bean.Person;
import com.vaadin.tokka.ui.components.fields.AbstractField;
import com.vaadin.tokka.ui.components.fields.TextField;

public class BinderTest {

    Binder<Person> binder;

    TextField nameField;
    TextField ageField;

    Person p = new Person();

    Validator<String> notEmpty = Validator.from(val -> !val.isEmpty(),
            "Value cannot be empty");

    Converter<String, Integer> stringToInteger = Converter.from(
            Integer::valueOf, String::valueOf, e -> "Value must be a number");

    Validator<Integer> notNegative = Validator.from(x -> x >= 0,
            "Value must be positive");

    @Before
    public void setUp() {
        binder = new Binder<>();
        p.setFirstName("Johannes");
        p.setAge(32);
        nameField = new TextField();
        ageField = new TextField();
    }

    @Test
    public void testAddFieldAndBind() {
        binder.addField(nameField).bind(Person::getFirstName,
                Person::setFirstName);
        binder.bind(p);

        assertEquals("Johannes", nameField.getValue());
    }

    @Test
    public void testAddFieldShortcut() {
        bindName();
        assertEquals("Johannes", nameField.getValue());
    }

    @Test
    public void testValueChangeOnSave() {
        bindName();

        nameField.setValue("Teemu");
        assertEquals("Johannes", p.getFirstName());

        binder.save();
        assertEquals("Teemu", p.getFirstName());
    }

    @Test
    public void testAddFieldAfterBinding() {
        binder.bind(p);
        binder.addField(nameField, Person::getFirstName, Person::setFirstName);

        assertEquals("Johannes", nameField.getValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testSaveBeforeBindThrows() {
        binder.save();
    }

    @Test(expected = IllegalStateException.class)
    public void testSaveAfterUnbindThrows() {
        bindName();

        binder.unbind();
        nameField.setValue("Teemu");
        binder.save();
    }

    @Test
    public void testValidationEmptyNameFails() {
        bindNameNonEmpty();

        nameField.setValue("");
        binder.save();
        assertEquals("Johannes", p.getFirstName());
        assertEquals("Value cannot be empty", getError(nameField));
    }

    @Test
    public void testValidationNonEmptyNamePasses() {
        bindNameNonEmpty();

        nameField.setValue("Leif");
        binder.save();
        assertEquals("Leif", p.getFirstName());
        assertNull(nameField.getComponentError());
    }

    @Test
    public void testMultipleValidatorsEmptyNameFails() {
        bindNameTwoValidators();

        nameField.setValue("");
        binder.save();
        assertEquals("Johannes", p.getFirstName());
    }

    @Test
    public void testMultipleValidatorsTooLongNameFails() {
        bindNameTwoValidators();
        nameField.setValue("This Name Is Very Long Indeed");
        binder.save();
        assertEquals("Johannes", p.getFirstName());
    }

    @Test
    public void testMultipleValidatorsShortEnoughNamePasses() {
        bindNameTwoValidators();
        nameField.setValue("Ilia");
        binder.save();
        assertEquals("Ilia", p.getFirstName());
    }

    @Test
    public void testConversionToPresentation() {
        bindAge();
        assertEquals("32", ageField.getValue());
    }

    @Test
    public void testConversionToModelInvalidAgeFails() {
        bindAge();

        ageField.setValue("foo");
        binder.save();
        assertEquals(32, p.getAge());
        assertEquals("Value must be a number", getError(ageField));
    }

    @Test
    public void testConversionToModelValidAgePasses() {
        bindAge();

        ageField.setValue("33");
        binder.save();
        assertEquals(33, p.getAge());
        assertNull(ageField.getComponentError());
    }

    @Test
    public void testConversionToModelNegativeAgeFails() {
        bindAge();

        ageField.setValue("-5");
        binder.save();
        assertEquals(32, p.getAge());
        assertEquals("Value must be positive", getError(ageField));
    }

    @Test
    public void testBeforeConversionEmptyAgeFails() {
        bindAge();

        ageField.setValue("");
        binder.save();
        assertEquals(32, p.getAge());
        assertEquals("Value cannot be empty", getError(ageField));
    }

    @Test
    public void testNoFieldsSavedOnInvalidValue() {
        bindName();
        bindAge();

        nameField.setValue("Henri");
        ageField.setValue("invalid");
        binder.save();
        assertEquals("Johannes", p.getFirstName());
        assertEquals(32, p.getAge());
    }

    private void bindName() {
        binder.addField(nameField, Person::getFirstName, Person::setFirstName);
        binder.bind(p);
    }

    private void bindNameNonEmpty() {
        binder.addField(nameField).addValidator(notEmpty)
                .bind(Person::getFirstName, Person::setFirstName);
        binder.bind(p);
    }

    private void bindNameTwoValidators() {
        binder.addField(nameField)
                .addValidator(notEmpty)
                .addValidator(Validator.from(val -> val.length() < 20,
                        "Value must be shorter than 20 letters"))
                .bind(Person::getFirstName, Person::setFirstName);
        binder.bind(p);
    }

    private void bindAge() {
        binder.addField(ageField).addValidator(notEmpty)
                .setConverter(stringToInteger).addValidator(notNegative)
                .bind(Person::getAge, Person::setAge);
        binder.bind(p);
    }

    private String getError(AbstractField<?> field) {
        return ((UserError) field.getComponentError()).getMessage();
    }
}
