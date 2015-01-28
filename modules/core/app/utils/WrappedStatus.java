package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.api.mvc.Codec;
import play.core.j.JavaResults;
import play.mvc.Results;

/**
 * Created by Heaven on 2015/1/28.
 */
public class WrappedStatus extends Results.Status {
    private JsonNode jsonBody;

    public static WrappedStatus WrappedOk(JsonNode jsonNode) {
        return new WrappedStatus(JavaResults.Ok(), jsonNode, Codec.javaSupported("utf-8"));
    }

    public WrappedStatus(play.api.mvc.Results.Status status, JsonNode jsonNode, Codec codec) {
        super(status, jsonNode, codec);
        jsonBody = jsonNode;
    }

    public JsonNode getJsonBody() {
        return jsonBody;
    }
}
