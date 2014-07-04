package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Result;

import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static play.mvc.Results.ok;

/**
 * 工具类
 *
 * @author Zephyre
 */
public class Utils {
    private static Map<String, MongoClient> mongoClientMap = new HashMap<>();

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

    /**
     * 获得默认的MongoDB客户端对象。
     *
     * @return
     * @throws UnknownHostException
     */
    public static MongoClient getMongoClient() throws UnknownHostException {
        return getMongoClient("localhost", 27017);
    }

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


    /**
     * 默认的时区设置为Asia/Shanghai。
     *
     * @return
     */
    public static TimeZone getDefaultTimeZone() {
        return TimeZone.getTimeZone("Asia/Shanghai");
    }


    public static JsonNode bsonToJson(Object node) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        fmt.setTimeZone(getDefaultTimeZone());
        return bsonToJson(node, fmt);
    }

    public static JsonNode bsonToJson(Object node, DateFormat dateFmt) {

        if (node instanceof Date)
            return Json.toJson(dateFmt.format((Date) node));
        else if (node instanceof Calendar)
            return Json.toJson(dateFmt.format(((Calendar) node).getTime()));
        else if (node instanceof ObjectId)
            return Json.toJson(node.toString());
        else if (node instanceof BasicDBList) {
            List<JsonNode> jsonList = new ArrayList<>();
            BasicDBList nodeList = (BasicDBList) node;
            for (Object tmp : nodeList)
                jsonList.add(bsonToJson(tmp));
            return Json.toJson(jsonList);
        } else if (node instanceof DBObject) {
            DBObject nodeMap = (DBObject) node;
            ObjectNode jsonMap = Json.newObject();
            for (String key : nodeMap.keySet())
                jsonMap.put(key, bsonToJson(nodeMap.get(key)));
            return jsonMap;
        } else
            return (node == null ? null : Json.toJson(node));
    }

}
