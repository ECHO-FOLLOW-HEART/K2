package aizou.core;

import exception.AizouException;
import models.user.Credential;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import play.mvc.Http;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Heaven on 2014/12/19.
 */
public class Crypto {
    private static final String UTF8 = "UTF-8";
    private static final String HAMC_SHA256 = "HmacSHA256";
    private static final String LINE_BREAK = "\n";
    private static final String SHA256 = "SHA-256";
    public static final String TAOZI = "TAOZI";
    public static final String AUTHORIZATION = "Authorization";
    public static final String TIMESTAMP = "Timestamp";
    public static final String HEAD = "TAOZI-1-HMAC-SHA256";
    public static final String[] HEADER_LIST = new String[]{"Platform", "Timestamp", "UserId", "Version"};
    private static Log logger = LogFactory.getLog("crypto.inner.debug");

    /**
     * 使用 HAMC-SHA256 对key , Data进行加密
     *
     * @param macKey  加密的key
     * @param macData 加密的数据
     * @return 加密后的十六进制编码
     */
    public static String HmacSHA256(String macKey, String macData) {
        String hex = null;
        try {
            Mac mac = Mac.getInstance(HAMC_SHA256);
            byte[] keyBytes = macKey.getBytes(UTF8);
            byte[] dataBytes = macData.getBytes(UTF8);
            SecretKey secret = new SecretKeySpec(keyBytes, HAMC_SHA256.toUpperCase());
            mac.init(secret);
            byte[] doFinal = mac.doFinal(dataBytes);
            byte[] hexB = new Hex().encode(doFinal);
            hex = new String(hexB);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return hex;
    }

    /**
     * SHA-256加密
     *
     * @param data String
     * @return hex String
     */
    public static String SHA256(String data) {
        String ret = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256);
            byte[] hash = digest.digest(data.getBytes(UTF8));
            byte[] hexB = new Hex().encode(hash);
            ret = new String(hexB);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 字符串转码
     *
     * @param input 需要转换的字符串
     * @return 转换后的编码
     */
    public static String uriEncode(String input) {
        String ret = null;
        try {
            ret = URLEncoder.encode(input, UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean checkAuthorization(Http.Request request, String userId) {
        Long uid = Long.parseLong(userId);
        List<String> field = new ArrayList<>();
        field.add("secKey");
        boolean result = false;
        if (!checkHeader(request))
            return false;
        try {
            Credential credential = UserAPI.getCredentialByUserId(uid, field);
            String secKey = credential.getSecKey();
            String gottenSignature = request.getHeader("Authorization");
            String rightSignature = getSignature(request, secKey);
            logger.info("right  :" + rightSignature);
            logger.info("gotten :" + gottenSignature);
            result = rightSignature.equals(gottenSignature);
        } catch (AizouException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 检查Http.Request 是否包含相关字段
     * @param request Http.Request
     * @return true when request is valid, else return false.
     */
    private static boolean checkHeader(Http.Request request) {
        String signature = request.getHeader(AUTHORIZATION);
        if (signature == null || signature.isEmpty())
            return false;
        for (String key : HEADER_LIST) {
            String value = request.getHeader(key);
            if (value == null || value.isEmpty())
                return false;
        }
        return true;
    }

    /**
     * 根据Request获取StringToSign
     *
     * @param request HttpRequest
     * @return String
     */
    private static String getStringToSign(Http.Request request) {
        String ret = null;
        try {
            String timeStamp = request.getHeader(TIMESTAMP);
            logger.debug("timeStamp: " + timeStamp);
            String scope = getScope(request);
            logger.debug("scope: " + scope);
            String httpMethod = getHttpMethod(request);
            logger.debug("method: " + httpMethod);
            String canonicalURI = getCanonicalURI(request);
            logger.debug("canonicalURI: " + canonicalURI);
            String canonicalQueryString = getCanonicalQueryString(request);
            logger.debug("canonicalQueryString: " + canonicalQueryString);
            String canonicalHeader = getCanonicalHeader(request);
            logger.debug("canonicalHeader: " + canonicalHeader);
            String hashedPayload = getHashedPayload(request);
            logger.debug("hashedPayload: " + hashedPayload);

            String tmp = httpMethod + LINE_BREAK
                    + canonicalURI + LINE_BREAK
                    + canonicalQueryString + LINE_BREAK
                    + canonicalHeader + LINE_BREAK
                    + hashedPayload;

            ret = HEAD + timeStamp + scope + SHA256(tmp);
        } catch (EncoderException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 获得 Signing Key
     *
     * @param request         HttpRequest
     * @param secretAccessKey 用户token
     * @return String
     */
    private static String getSigningKey(Http.Request request, String secretAccessKey) {
        String timeStamp = request.getHeader(TIMESTAMP);
        return SHA256(TAOZI + timeStamp + secretAccessKey);
    }

    /**
     * get Signature
     *
     * @param request         HttpRequest
     * @param secretAccessKey String
     * @return String
     */
    public static String getSignature(Http.Request request, String secretAccessKey) {
        String stringToSign = getStringToSign(request);
        String signingKey = getSigningKey(request, secretAccessKey);
        return HmacSHA256(signingKey, stringToSign);
    }

    /**
     * get Scope
     * 当前定义的scope不包括 时间地点信息,仅包含请求
     *
     * @param request HttpRequest
     * @return path
     */
    private static String getScope(Http.Request request) {
        //<scope> = <request>
        return request.path();
    }

    /**
     * 获得payLoad 的哈希结果
     *
     * @param request HttpRequest
     * @return empty string
     */
    private static String getHashedPayload(Http.Request request) {
        return SHA256(uriEncode(request.body().toString()));
    }

    /**
     * 获得CanonicalHeader 的 uriEncode 结果
     * 只检查以下字段: Platform, Version, UserId, Timestamp
     * @param request HttpRequest
     * @return String
     */
    private static String getCanonicalHeader(Http.Request request) {
        Map<String, String[]> headerMap = request.headers();
        StringBuilder headerBuilder = new StringBuilder();
        for (String key : HEADER_LIST) {
            //如果同一名称对应多个值，则只取第一个
            headerBuilder.append(key);
            headerBuilder.append(":");
            headerBuilder.append(headerMap.get(key)[0].toLowerCase());
            headerBuilder.append(LINE_BREAK);
        }
        return headerBuilder.toString();
    }

    /**
     * 获得 Canonical Query String 的 uri encode 结果
     *
     * @param request HttpRequest
     * @return String
     * @throws UnsupportedEncodingException
     * @throws EncoderException
     */
    private static String getCanonicalQueryString(Http.Request request) throws UnsupportedEncodingException, EncoderException {
        Map<String, String[]> queryStringMap = request.queryString();
        ArrayList<Map.Entry<String, String[]>> queryList = getSortedList(queryStringMap);
        StringBuilder queryBuilder = new StringBuilder();
        Boolean first = true;
        for (Map.Entry<String, String[]> que : queryList) {
            if (!first) {
                queryBuilder.append("&");
            } else {
                first = false;
            }
            queryBuilder.append(uriEncode(que.getKey()));
            queryBuilder.append("=");
            // 如果同一名称对应多个参数，则只取第一个
            queryBuilder.append(uriEncode(que.getValue()[0]));
        }
        return queryBuilder.toString();
    }

    /**
     * 获得Http Method 的大写结果
     *
     * @param request HttpRequest
     * @return "GET"|"POST"|"PUT"...
     */
    private static String getHttpMethod(Http.Request request) {
        return request.method().toUpperCase();
    }

    /**
     * 获得 canonical uri
     *
     * @param request HttpRequest
     * @return String
     * @throws UnsupportedEncodingException
     * @throws EncoderException
     */
    private static String getCanonicalURI(Http.Request request) throws UnsupportedEncodingException, EncoderException {
        return uriEncode(request.path());
    }

    /**
     * 将Map根据Key字段进行排序
     *
     * @param map request的header或者queryString
     * @return sorted ArrayList
     */
    private static ArrayList<Map.Entry<String, String[]>> getSortedList(Map<String, String[]> map) {
        ArrayList<Map.Entry<String, String[]>> entryList = new ArrayList<>();
        entryList.addAll(map.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, String[]>>() {
            @Override
            public int compare(Map.Entry<String, String[]> o1, Map.Entry<String, String[]> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        return entryList;
    }
}
