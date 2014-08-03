package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import exception.ErrorCode;
import exception.TravelPiException;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
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
    private static Datastore datastore = null;
    private static Morphia morphia = null;

    private synchronized static void initMongoDB() throws TravelPiException {
        String dbName = "travelpi";
        morphia = new Morphia();
        try {
            datastore = morphia.createDatastore(new MongoClient(), dbName);
        } catch (UnknownHostException e) {
            throw new TravelPiException(ErrorCode.DATABASE_ERROR, "Cannot initialize the MongoDB client.");
        }
    }

    public synchronized static Morphia getMorphia() throws TravelPiException {
        if (morphia == null)
            initMongoDB();
        return morphia;
    }

    public synchronized static Datastore getDatastore() throws TravelPiException {
        if (datastore == null)
            initMongoDB();
        return datastore;
    }

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
    public static MongoClient getMongoClient() throws TravelPiException {
        return getMongoClient("localhost", 27017);
    }

    /**
     * 获得MongoDB客户端对象
     *
     * @param host
     * @param port
     * @return
     */
    public static MongoClient getMongoClient(String host, int port) throws TravelPiException {
        String key = host + String.valueOf(port);
        MongoClient client = mongoClientMap.get(key);
        if (client != null)
            return client;

        try {
            client = new MongoClient(host, port);
        } catch (UnknownHostException e) {
            throw new TravelPiException(ErrorCode.DATABASE_ERROR, String.format("Invalid database connection: host=%s, port=%d", host, port));
        }
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

    /**
     * 根据经纬度计算距离，单位是公里
     * @param lat1
     * @param lat2
     * @param lon1
     * @param lon2
     * @return
     */
    public static int getDistatce(double lat1, double lat2, double lon1,    double lon2) {
        double R = 6371;
        double distance = 0.0;
        double dLat = (lat2 - lat1) * Math.PI / 180;
        double dLon = (lon2 - lon1) * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1 * Math.PI / 180)
                * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        distance = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))) * R;
        int result = Integer.parseInt(new java.text.DecimalFormat("0").format(distance));
        return result;
    }
}
