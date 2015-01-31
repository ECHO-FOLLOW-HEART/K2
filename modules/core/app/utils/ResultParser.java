package utils;

import exception.ErrorCode;

import static utils.WrappedStatus.WrappedOk;

/**
 * Created by Heaven on 2015/1/30.
 */
public class ResultParser implements SerializeParser {
    @Override
    public Object dSerializing(String inputStr) {
        String ret = String.format("{\"lastModified\":%d, \"result\":%s, \"code\":%d}",
                System.currentTimeMillis() / 1000, inputStr, ErrorCode.NORMAL);
        return WrappedOk(ret).as("application/json;charset=utf-8");
    }

    @Override
    public String Serializing(Object inputObj) {
        if (!(inputObj instanceof WrappedStatus))
            throw new java.lang.IllegalArgumentException(inputObj.getClass().toString());
        return ((WrappedStatus) inputObj).getStringBody();
    }
}
