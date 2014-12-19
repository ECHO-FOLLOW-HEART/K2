import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Hex;
import play.mvc.Http;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by Heaven on 2014/12/19.
 */
// TODO 添加SHA256加密算法
public class Crypto {
    private static final String UTF8 = "UTF-8";
    private static final String HAMC_SHA256 = "HmacSHA256";
    private static final String LINE_BREAK = "\n";
    private static final String TIMESTAMP = "timestamp";
    private static final String HEAD = "TAOZI-1-HMAC-SHA256";
    private static final String SHA256 = "SHA-256";
    private static final String TAOZI = "TAOZI";

    /**
     * 使用 HAMC-SHA256 对key , Data进行加密
     * @param macKey 加密的key
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
     * @param input 需要转换的字符串
     * @param encodeSlash 是否转换符号'/'
     * @return 转换后的编码
     * @throws EncoderException
     * @throws UnsupportedEncodingException
     */
    public static String uriEncode(String input, Boolean encodeSlash) throws EncoderException, UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (('A'<=ch && ch<='Z') ||
                    ('a'<=ch && ch<='z') ||
                    ('0'<=ch && ch<='9') ||
                    ch=='_' || ch=='-' || ch=='~' || ch=='.') {
                builder.append(ch);
            } else if (ch=='/') {
                builder.append(encodeSlash?"%2F":ch);
            } else {
                byte[] hexB = new Hex().encode((""+ch).getBytes(UTF8));
                builder.append(new String(hexB));
            }
        }
        return builder.toString();
    }

    /**
     * 根据Request获取StringToSign
     * @param request HttpRequest
     * @return String
     */
    public static String getStringToSign(Http.Request request) {
        String ret = null;
        try {
            String timeStamp = request.getHeader(TIMESTAMP);
            String scope = getScope(request);
            String httpMethod = getHttpMethod(request);
            String canonicalURI = getCanonicalURI(request);
            String canonicalQueryString = getCanonicalQueryString(request);
            String canonicalHeader = getCanonicalHeader(request);
            String hashedPayload = getHashedPayload(request);
            
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

    public static String getSigningKey(Http.Request request, String secretAccessKey) {
        String timeStamp = request.getHeader(TIMESTAMP);
        return SHA256(TAOZI + timeStamp + secretAccessKey);
    }

    public static String getSignature(Http.Request request, String secretAccessKey) {
        String stringToSign = getStringToSign(request);
        String signingKey = getSigningKey(request, secretAccessKey);
        return HmacSHA256(signingKey, stringToSign);
    }

    private static String getScope(Http.Request request) {
        //<scope> = <request>
        return request.path();
    }

    /**
     * 获得payLoad 的哈希结果
     * @param request HttpRequest
     * @return empty string
     */
    private static String getHashedPayload(Http.Request request) {
        //当前暂时不考虑payLoad的问题
        return "";
    }

    /**
     * 获得CanonicalHeader 的 uriEncode 结果
     * @param request HttpRequest
     * @return String
     */
    private static String getCanonicalHeader(Http.Request request) {
        Map<String, String[]> headerMap = request.headers();
        ArrayList<Map.Entry<String, String[]>> headerList = getSortedList(headerMap);
        StringBuilder headerBuilder = new StringBuilder();
        for (Map.Entry<String, String[]> head : headerList) {
            headerBuilder.append(head.getKey().toLowerCase());
            headerBuilder.append(":");
            //如果同一名称对应多个值，则只取第一个
            headerBuilder.append(head.getValue()[0].trim());
            headerBuilder.append(LINE_BREAK);
        }
        return headerBuilder.toString();
    }

    /**
     * 获得 Canonical Query String 的 uri encode 结果
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
            queryBuilder.append(uriEncode(que.getKey(), true));
            queryBuilder.append("=");
            // 如果同一名称对应多个参数，则只取第一个
            queryBuilder.append(uriEncode(que.getValue()[0], true));
        }
        return queryBuilder.toString();
    }

    /**
     * 获得Http Method 的大写结果
     * @param request HttpRequest
     * @return "GET"|"POST"|"PUT"...
     */
    private static String getHttpMethod(Http.Request request) {
        return request.method().toUpperCase();
    }

    /**
     * 获得 canonical uri
     * @param request HttpRequest
     * @return String
     * @throws UnsupportedEncodingException
     * @throws EncoderException
     */
    private static String getCanonicalURI(Http.Request request) throws UnsupportedEncodingException, EncoderException {
        return uriEncode(request.path(), false);
    }

    /**
     * 将Map根据Key字段进行排序
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
