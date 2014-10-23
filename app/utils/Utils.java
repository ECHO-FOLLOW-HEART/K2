package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import exception.ErrorCode;
import exception.TravelPiException;
import org.apache.commons.codec.binary.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultText;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import play.Configuration;
import play.libs.Json;
import play.mvc.Result;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        jsonObj.put("debug", msg);
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
     * @param errCode    错误码
     * @param msg        提示信息
     * @param isGotByApp 标识Message是否被手机端直接获得并显示出来
     * @return
     */
    public static Result createResponse(int errCode, String msg, boolean isGotByApp) {
        ObjectNode jsonObj = Json.newObject();
        jsonObj.put("message", msg);
        return createResponse(errCode, jsonObj);
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
     * 根据经纬度计算两点距离，单位是公里
     *
     * @param lat1
     * @param lat2
     * @param lon1
     * @param lon2
     * @return
     */
    public static int getDistatce(double lat1, double lat2, double lon1, double lon2) {
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

    /**
     * 获得某用户登录签名
     *
     * @return
     */
    public static String getSecToken() {
        Long r = Math.abs(new Random().nextLong()) + System.currentTimeMillis();
        return toSha1Hex(r.toString());
    }

    /**
     * SHA1摘要函数
     *
     * @param msg
     * @return
     */
    public static String toSha1Hex(String msg) {
        try {
            byte[] ret = MessageDigest.getInstance("SHA-256").digest(msg.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte aB : ret) {
                String hex = Integer.toHexString(aB & 0xFF);
                if (hex.length() == 1)
                    hex = '0' + hex;
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ignored) {
            return null;
        }
    }
    /**
     * 生成token
     * @return
     */
    public static String createToken() {
        String ram = ((Integer) (new Random()).nextInt(Integer.MAX_VALUE)).toString();
        try {
            return Base64.encodeBase64String(MessageDigest.getInstance("SHA-256").digest(ram.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 生成随机字符串
     *
     * @param length 生成字符串的长度
     * @return
     */
    public static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        int size = base.length();
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(base.charAt(random.nextInt(size)));
        return sb.toString();
    }

    /**
     * 取得密盐
     */
    public static String getSalt() {
        return getRandomString(32);
    }

    /**
     * 解析电话号码字符串
     *
     * @param tel 输入的电话号码。
     * @return 电话号码："13800138000"。如果不符合格式规范，则返回null。
     */
    public static String telParser(String tel) {
        if (tel == null)
            return null;

        // 去掉所有的空格，横线，括号等
        Pattern pattern = Pattern.compile("[\\s\\-\\(\\)]+");
        String newTel = pattern.matcher(tel).replaceAll("");
        Matcher matcher = Pattern.compile("\\d{11}").matcher(newTel);

        // 如果电话号码不符合规范，则返回null。
        return matcher.find() ? newTel : null;
    }

    /**
     * 发送短信
     *
     * @param recipients 接受列表
     * @param msg        短信内容
     */
    public static void sendSms(List<String> recipients, String msg) throws TravelPiException {
        Configuration config = Configuration.root();
        Map<String, Object> sms = config.getConfig("sms").asMap();
        String host = sms.get("host").toString();
        int port = Integer.parseInt(sms.get("port").toString());
        String user = sms.get("user").toString();
        String passwd = sms.get("passwd").toString();


        SAXReader saxReader = new SAXReader();
        try {
            StringBuilder builder = new StringBuilder();
            for (byte b : msg.getBytes(Charset.forName("GBK"))) {
                builder.append("%");
                builder.append(Integer.toHexString(b & 0xFF));
            }
            String url = String.format(
                    "http://%s:%d/QxtSms/QxtFirewall?OperID=%s&OperPass=%s&DesMobile=%s&Content=%s&ContentType=15",
                    host, port, user, passwd, StringUtils.join(recipients, ","), builder.toString());
            Document document = saxReader.read(new URL(url));

            String xpath = "/response/code/text()";
            List l = document.selectNodes(xpath);
            if (l.isEmpty())
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Cannot find any return codes.");
            int code = Integer.parseInt(((DefaultText) l.get(0)).getText());
            if (code != 3 && code != 1)
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, String.format("Error in sending sms. code: %d.", code));
        } catch (DocumentException | MalformedURLException | IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Error in sending sms.");
        }
    }
}
