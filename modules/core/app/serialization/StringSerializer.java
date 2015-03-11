package serialization;

/**
 * Created by zephyre on 2/14/15.
 */
public class StringSerializer implements SerializeParser<String> {
    @Override
    public String deserialize(String inputStr) {
        return inputStr;
    }

    @Override
    public String serialize(String inputObj) {
        return inputObj;
    }
}
