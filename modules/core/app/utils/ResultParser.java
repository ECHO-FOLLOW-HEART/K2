package utils;

import serialization.SerializeParser;

import static utils.WrappedStatus.WrappedOk;

/**
 * Created by Heaven on 2015/1/30.
 */
public class ResultParser implements SerializeParser {
    @Override
    public Object deserialize(String inputStr) {
        return WrappedOk(inputStr);
    }

    @Override
    public String serialize(Object inputObj) {
        if (!(inputObj instanceof WrappedStatus))
            throw new java.lang.IllegalArgumentException(inputObj.getClass().toString());
        return ((WrappedStatus) inputObj).getStringBody();
    }
}
