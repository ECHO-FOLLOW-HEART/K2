package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 2/10/15.
 */
public class IntegerValidator extends NumberValidator {

    NumberValidator validator;

    public IntegerValidator() {
        this(null);
    }

    public IntegerValidator(NumberValidator validator) {
        this.validator = validator;
    }

    @Override
    public void validate(JsonNode item) {
        assertThat(item.isIntegralNumber()).isTrue();
        if (validator != null)
            validator.validate(item);
    }
}