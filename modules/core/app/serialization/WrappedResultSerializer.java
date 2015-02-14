package serialization;

import utils.WrappedStatus;

/**
 * Created by zephyre on 2/13/15.
 */
public class WrappedResultSerializer implements SerializeParser<WrappedStatus> {
    @Override
    public WrappedStatus deserialize(String inputStr) {
        return WrappedStatus.WrappedOk(inputStr);
    }

    @Override
    public String serialize(WrappedStatus inputObj) {
        return inputObj.getStringBody();
    }
}
