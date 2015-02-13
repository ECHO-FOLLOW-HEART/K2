package utils.results;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import play.mvc.Result;
import utils.WrappedStatus;

/**
 * Created by zephyre on 2/13/15.
 */
public class TaoziResBuilder implements ResultBuilder {
    private ErrorCode code;
    private String debugInfo;
    private String message;
    private String body;

    public TaoziResBuilder() {
        code = ErrorCode.NORMAL;
    }

    @Override
    public Result build() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(String.format("\"lastModified\":%d", System.currentTimeMillis()));
        builder.append(String.format(",\"code\":%d", code.getVal()));
        builder.append(String.format(",\"status\":\"%s\"", code.toString()));

        if (debugInfo != null)
            builder.append(String.format(",\"debug\":\"%s\"", debugInfo));

        if (code != ErrorCode.NORMAL)
            builder.append(String.format(",\"message\":\"%s\"", message != null ? message : ""));

        builder.append(String.format(",\"result\":%s", body != null ? body : "{}"));

        builder.append("}");

        String res = builder.toString();

        return WrappedStatus.WrappedOk(res);
    }

    @Override
    public ResultBuilder setCode(ErrorCode code) {
        this.code = code;
        return this;
    }

    @Override
    public ResultBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public ResultBuilder setBody(JsonNode body) {
        return setBody(body.toString());
    }

    @Override
    public ResultBuilder setDebugInfo(String debugInfo) {
        this.debugInfo = debugInfo;
        return this;
    }

    @Override
    public ResultBuilder setMessage(String message) {
        this.message = message;
        return this;
    }
}
