package controllers.mom;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Heaven on 2015/3/26.
 */
public class JsonMessage implements Message {

    private JSONObject jsonObject = null;

    public JsonMessage() {
        this.jsonObject = new JSONObject();
    }

    /**
     * 生成Message
     * @return
     */
    public static JsonMessage obtain() {
        return new JsonMessage();
    }

    public static JsonMessage obtainWithTimeStamp() {
        return new JsonMessage().with("timeStamp", System.currentTimeMillis());
    }

    public JsonMessage with(String key, Object value) {
        try {
            this.jsonObject.put(key, value);
        } catch (JSONException ignored) {
        }
        return this;
    }

    @Override
    public byte[] toJsonBytes() throws JSONException {
        return jsonObject.toString().getBytes();
    }
}
