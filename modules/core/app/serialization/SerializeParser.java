package serialization;

/**
 * Created by Heaven on 2015/1/30.
 */
public interface SerializeParser<T> {
    public T deserialize(String inputStr);
    public String serialize(T inputObj);
}
