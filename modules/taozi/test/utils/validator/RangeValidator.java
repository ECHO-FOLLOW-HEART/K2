package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 2/10/15.
 */
public class RangeValidator extends NumberValidator {

    NumberValidator validator;

    Double min;

    Double max;

    boolean[] exclusive;

    public RangeValidator(Double min, Double max, boolean[] exclusive) {
        this(null, min, max, exclusive);
    }

    public RangeValidator(NumberValidator validator, Double min, Double max, boolean[] exclusive) {
        this.validator = validator;
        this.min = min;
        this.max = max;
        this.exclusive = exclusive;
        if (this.exclusive == null || this.exclusive.length != 2)
            this.exclusive = new boolean[]{false, false};
    }

    @Override
    public void validate(JsonNode item) {
        double val = item.asDouble();

        if (min != null) {
            if (exclusive[0])
                assertThat(val > min).isTrue();
            else
                assertThat(val >= min).isTrue();
        }

        if (max != null) {
            if (exclusive[1])
                assertThat(val < max).isTrue();
            else
                assertThat(val <= max).isTrue();
        }

        if (validator != null)
            validator.validate(item);
    }
}
