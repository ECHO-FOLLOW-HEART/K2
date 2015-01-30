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

    public static WrappedStatus WrappedOk(JsonNode jsonNode) {
        WrappedStatus ret = new WrappedStatus(JavaResults.Ok(), jsonNode, Codec.javaSupported("utf-8"));
        ret.jsonBody = jsonNode;
        ret.stringBody = jsonNode.toString();
        return ret;
    }

    public static WrappedStatus WrappedOk(String msg) {
        WrappedStatus ret = new WrappedStatus(JavaResults.Ok(), msg, Codec.javaSupported("utf-8"));
        ret.jsonBody = Json.toJson(msg);
        ret.stringBody = msg;
        return ret;
    }

    public static WrappedStatus MiscWrappedStatus(int status) {
        return new WrappedStatus(JavaResults.Status(status));
    }

    private WrappedStatus(play.api.mvc.Results.Status status) {
        super(status);
    }

    private WrappedStatus(play.api.mvc.Results.Status status, JsonNode jsonNode, Codec codec) {
        super(status, jsonNode, codec);
    }

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
