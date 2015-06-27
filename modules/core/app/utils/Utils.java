package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import exception.AizouException;
import exception.ErrorCode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.util.DateUtil;
import org.bson.types.ObjectId;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultText;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import play.Configuration;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.WrappedStatus.WrappedOk;

/**
 * 工具类
 *
 * @author Zephyre
 */
public class Utils {
    private static Map<String, MongoClient> mongoClientMap = new HashMap<>();
    private static Datastore datastore = null;
    private static Morphia morphia = null;

    private synchronized static void initMongoDB() throws AizouException {
        String dbName = "travelpi";
        morphia = new Morphia();
        datastore = morphia.createDatastore(new MongoClient(), dbName);
    }

    public synchronized static Morphia getMorphia() throws AizouException {
        if (morphia == null)
            initMongoDB();
        return morphia;
    }

    public synchronized static Datastore getDatastore() throws AizouException {
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

    public static Result createResponse(ErrorCode errCode) {
        return createResponse(errCode, "");
    }

    public static Result status(ErrorCode errCode, String msg) {
        String ret = String.format("{\"lastModified\":%d, \"result\":%s, \"code\":%d}",
                System.currentTimeMillis() / 1000, msg, errCode.getVal());
//        return ok(ret).as("application/json;charset=utf-8");
        return WrappedOk(ret).as("application/json;charset=utf-8");
    }

    public static Result status(String msg) {
        return status(ErrorCode.NORMAL, msg);
    }

    /**
     * 建立Result对象
     *
     * @param errCode
     * @param debugMessage Debug信息
     * @return
     */
    public static Result createResponse(ErrorCode errCode, String debugMessage) {
        ObjectNode jsonObj = Json.newObject();
        if (debugMessage != null)
            jsonObj.put("debug", debugMessage);
        return createResponse(errCode, jsonObj);
    }


    public static Result createResponse(ErrorCode errCode, JsonNode result) {
        ObjectNode response = Json.newObject();
        response.put("lastModified", System.currentTimeMillis() / 1000);
        if (errCode.getVal() == 0) {
            if (result != null)
                response.put("result", result);
            response.put("code", 0);
        } else {
            response.put("code", errCode.getVal());
            if (result != null)
                response.put("err", result);
        }
//        return ok(response);
        return WrappedOk(response);
    }

    public static Result createResponse(Http.Response rsp, String lastModify, int errCode, JsonNode result) throws ParseException {
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
        //SimpleDateFormat formatGMT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        //.formatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
        //rsp.setHeader("Last-Modify", formatGMT.format(DateUtil.parseDate(lastModify)));
        //rsp.setHeader("Cache-control", "public");
//        return ok(response);
        return WrappedOk(response);
    }

    /**
     * @param errCode    错误码
     * @param msg        提示信息
     * @param isGotByApp 标识Message是否被手机端直接获得并显示出来
     * @return
     */
    public static Result createResponse(ErrorCode errCode, String msg, boolean isGotByApp) {
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
    public static MongoClient getMongoClient() throws AizouException {
        return getMongoClient("localhost", 27017);
    }

    /**
     * 获得MongoDB客户端对象
     *
     * @param host
     * @param port
     * @return
     */
    public static MongoClient getMongoClient(String host, int port) throws AizouException {
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
     *
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
     * 生成签名数据
     *
     * @param data 待加密的数据
     * @param key  加密使用的key
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static byte[] hmac_sha1(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        return mac.doFinal(data.getBytes());
        //return Hex.encodeHexString(rawHmac);
    }

    /**
     * URL安全的Base64编码，手动加“=”
     *
     * @param str
     * @return
     */
    public static String base64Padding(String str) {
        int len = str.length();
        int cnt = len % 4;
        if (cnt == 2)
            return str + "==";
        else if (cnt == 3)
            return str + "=";
        else
            return str;
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
    public static void sendSms(List<String> recipients, String msg) throws AizouException {
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
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "Cannot find any return codes.");
            int code = Integer.parseInt(((DefaultText) l.get(0)).getText());
            if (code != 3 && code != 1)
                throw new AizouException(ErrorCode.UNKOWN_ERROR, String.format("Error in sending sms. code: %d.", code));
        } catch (DocumentException | MalformedURLException | IllegalArgumentException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "Error in sending sms.");
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isNumeric(Collection<?> list) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum;
        for (Object str : list) {
            isNum = pattern.matcher(str.toString());
            if (!isNum.matches()) {
                return false;
            }
        }
        return true;
    }

    public static final String CACHE_CONTROLKEY = "Cache-control";
    public static final String IF_MODIFY_SINCE = "If-Modified-Since";

    /**
     * 判断是否使用App端缓存
     *
     * @param request    Http请求
     * @param lastModify 服务器资源的修改时间 "2015 01 17 00:00:00"
     * @return
     */
    public static boolean useCache(Http.Request request, String lastModify) {

        String cacheControl = request.hasHeader(CACHE_CONTROLKEY) ? request.getHeader(CACHE_CONTROLKEY) : request.getHeader(CACHE_CONTROLKEY.toLowerCase());
        if (cacheControl == null || cacheControl.equals("") || cacheControl.toLowerCase().equals("no-cache"))
            return false;
        String ifModifiedSince = request.hasHeader(IF_MODIFY_SINCE) ? request.getHeader(IF_MODIFY_SINCE) : request.getHeader(IF_MODIFY_SINCE.toLowerCase());
        if (lastModify == null || ifModifiedSince == null || ifModifiedSince.equals(""))
            return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
//        SimpleDateFormat formatGMT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
//        formatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date serverDate;
        Date appDate;
        try {
            serverDate = format.parse(lastModify);
            LogUtils.info(Utils.class, lastModify);
            appDate = DateUtil.parseDate(ifModifiedSince);
        } catch (ParseException e) {
            return false;
        }
        return !appDate.before(serverDate);
    }

    public static void addCacheResponseHeader(Http.Response rsp, String lastModify) throws ParseException {

        SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat formatGMT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        formatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
        rsp.setHeader("Last-Modify", formatGMT.format(format.parse(lastModify)));
        rsp.setHeader("Cache-control", "public");

        Calendar cal = Calendar.getInstance();
        cal.setTime(format.parse(lastModify));
        cal.add(Calendar.DATE, +1);
        rsp.setHeader("Expires", formatGMT.format(cal.getTime()));
    }

    public static String getReqValue(JsonNode req, String key, String defaultValue) {
        return req.has(key) ? req.get(key).asText() : defaultValue;
    }
}
