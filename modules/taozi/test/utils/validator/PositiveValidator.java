package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 2/10/15.
 */
public class PositiveValidator extends NumberValidator {

    NumberValidator validator;

    boolean exclusive;

    public PositiveValidator(NumberValidator validator) {
        this(validator, false);
    }

    public PositiveValidator(NumberValidator validator, boolean excludeZero) {
        this.validator = validator;
        this.exclusive = excludeZero;
    }

    @Override
    public void validate(JsonNode item) {
        double val = item.asDouble();
        if (exclusive)
            assertThat(val > 0).isTrue();
        else
            assertThat(val >= 0).isTrue();

        if (validator != null)
            validator.validate(item);
    }
}
