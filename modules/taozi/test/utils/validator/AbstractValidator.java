package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by zephyre on 2/11/15.
 */
public interface AbstractValidator {
    public abstract void validate(JsonNode item);
}
