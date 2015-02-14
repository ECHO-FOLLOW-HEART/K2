package utils.results;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import play.mvc.Result;

/**
 * 通过stream风格的调用，构造Result对象
 *
 * Created by zephyre on 2/13/15.
 */
public interface ResultBuilder {
    /**
     * 返回Result对象
     */
    public Result build();

    /**
     * 设置ErrorCode
     */
    public ResultBuilder setCode(ErrorCode code);

    /**
     * 设置Result对象的数据内容
     */
    public ResultBuilder setBody(String body);

    /**
     * 设置Result对象的数据内容
     */
    public ResultBuilder setBody(JsonNode body);

    /**
     * 设置调试信息
     */
    public ResultBuilder setDebugInfo(String debugInfo);

    /**
     * 设置错误帮助文案信息
     */
    public ResultBuilder setMessage(String message);
}
