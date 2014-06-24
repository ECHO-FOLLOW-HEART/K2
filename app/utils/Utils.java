package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import play.libs.Json;
import play.mvc.Result;

import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.HashMap;
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

    private static Map<String, MongoClient> mongoClientMap = new HashMap<>();

    /**
     * 获得MongoDB客户端对象
     *
     * @param host
     * @param port
     * @return
     */
    public static MongoClient getMongoClient(String host, int port) throws UnknownHostException {
        String key = host + String.valueOf(port);
        MongoClient client = mongoClientMap.get(key);
        if (client != null)
            return client;

        client = new MongoClient(host, port);
        mongoClientMap.put(key, client);
        return client;
    }

}
