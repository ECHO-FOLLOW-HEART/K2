package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 断言一个JsonNode是否为有效的数字
 *
 * Created by zephyre on 2/10/15.
 */
public abstract class NumberValidator {
    public abstract void validate(JsonNode item);
}
