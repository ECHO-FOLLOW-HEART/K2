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

    public static WrappedStatus WrappedOk(JsonNode jsonNode) {
        return new WrappedStatus(JavaResults.Ok(), jsonNode, Codec.javaSupported("utf-8"));
    }

    public static WrappedStatus WrappedOk(String msg) {
        return new WrappedStatus(JavaResults.Ok(), msg, Codec.javaSupported("utf-8"));
    }

    public static WrappedStatus MiscWrappedStatus(int status) {
        return new WrappedStatus(JavaResults.Status(status));
    }

    public WrappedStatus(play.api.mvc.Results.Status status) {
        super(status);
    }

    public WrappedStatus(play.api.mvc.Results.Status status, JsonNode jsonNode, Codec codec) {
        super(status, jsonNode, codec);
        jsonBody = jsonNode;
    }

    public WrappedStatus(play.api.mvc.Results.Status status, String msg, Codec codec) {
        super(status, msg, codec);
        jsonBody = Json.parse(msg);
    }

    public JsonNode getJsonBody() {
        return jsonBody;
    }
}
