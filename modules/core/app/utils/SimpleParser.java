package utils;

/**
 * Created by Heaven on 2015/1/31.
 */
public class SimpleParser implements SerializeParser {

    private final Class<?> parserClass;

    SimpleParser(Class<?> aClass) {
        parserClass = aClass;
    }

    @Override
    public Object dSerializing(String inputStr) {
        if (parserClass.equals(Integer.class) || parserClass.equals(int.class)) {
            return Integer.parseInt(inputStr);
        } else if (parserClass.equals(Long.class) || parserClass.equals(long.class)) {
            return Long.parseLong(inputStr);
        } else if (parserClass.equals(Double.class) || parserClass.equals(double.class)) {
            return Double.parseDouble(inputStr);
        } else if (parserClass.equals(Boolean.class) || parserClass.equals(boolean.class)) {
            return Boolean.parseBoolean(inputStr);
        }
        return parserClass.cast(inputStr);
    }

    @Override
    public String Serializing(Object inputObj) {
        return inputObj.toString();
    }
}
