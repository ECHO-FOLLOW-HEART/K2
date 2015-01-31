package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.api.mvc.Codec;
import play.core.j.JavaResults;
import play.libs.Json;
import play.mvc.Results;

/**
 * Created by Heaven on 2015/1/28.
 */
public class WrappedStatus extends Results.Status {
    private JsonNode jsonBody = null;
    private String stringBody = null;

    /**
     * get Wrapped Status
     * @param jsonNode
     * @return
     */
    public static WrappedStatus WrappedOk(JsonNode jsonNode) {
        String msg = jsonNode.toString();
        WrappedStatus ret = new WrappedStatus(JavaResults.Ok(), msg, Codec.javaSupported("utf-8"));
        ret.jsonBody = jsonNode;
        ret.stringBody = msg;
        return ret;
    }

    /**
     * get Wrapped status
     * 注意：该函数应该只再缓存命中的情况下使用，否则会增加不必要的序列化开销
     * @param msg
     * @return
     */
    public static WrappedStatus WrappedOk(String msg) {
        WrappedStatus ret = new WrappedStatus(JavaResults.Ok(), msg, Codec.javaSupported("utf-8"));
        ret.jsonBody = Json.toJson(msg);
        ret.stringBody = msg;
        return ret;
    }

    /**
     * get assigned Status with empty body
     * @param status 指定的status, 如304
     * @return
     */
    public static WrappedStatus MiscWrappedStatus(int status) {
        return new WrappedStatus(JavaResults.Status(status));
    }

    private WrappedStatus(play.api.mvc.Results.Status status) {
        super(status);
    }

//    private WrappedStatus(play.api.mvc.Results.Status status, JsonNode jsonNode, Codec codec) {
//        super(status, jsonNode, codec);
//    }

    private WrappedStatus(play.api.mvc.Results.Status status, String msg, Codec codec) {
        super(status, msg, codec);
    }

    public JsonNode getJsonBody() {
        return jsonBody;
    }

    public String getStringBody() {
        return stringBody;
    }
}
