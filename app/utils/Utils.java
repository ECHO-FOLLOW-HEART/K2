package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Result;

import java.lang.reflect.Field;
import java.util.Map;

import static play.mvc.Results.ok;

/**
 * 工具类
 *
 * @author Zephyre
 */
public class Utils {
    public static Object create(Class c, Map<String, Object> kvPairs) {
        Object obj = null;
        try {
            obj = c.newInstance();
            for (String key : kvPairs.keySet()) {
                Object value = kvPairs.get(key);
                Field field = c.getDeclaredField(key);
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Result createResponse(int errCode, String msg) {
        ObjectNode jsonObj = Json.newObject();
        jsonObj.put("msg", msg);
        return createResponse(errCode, jsonObj);
    }

    public static Result createResponse(int errCode, JsonNode result) {
        ObjectNode response = Json.newObject();
        response.put("lastModified", System.currentTimeMillis() / 1000);
        if (errCode == 0) {
            if (result != null)
                response.put("result", result);
            response.put("code", 0);
        } else {
            response.put("code", errCode);
            if (result != null)
                response.put("err", result);
        }
        return ok(response);
    }

}
