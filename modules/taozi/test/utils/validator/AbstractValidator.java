package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by zephyre on 2/12/15.
 */
public interface AbstractValidator {
    public void validate(JsonNode item);
}
