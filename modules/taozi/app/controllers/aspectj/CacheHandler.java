package controllers.aspectj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import play.Configuration;
import play.cache.Cache;
import play.mvc.Http;
import play.mvc.Result;
import serialization.ParserFactory;
import serialization.SerializeParser;
import utils.WrappedStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;

/**
 * Created by Heaven on 2015/1/22.
 */
@Aspect
public class CacheHandler {
    private static final java.lang.String SEPARATOR = "\\|";
    public static int MAX_KEY_LENGTH = 1000;        //1KB
    public static int MAX_VALUE_LENGTH = 1000000;   //1MB
    Log logger = LogFactory.getLog(this.getClass());

    private boolean cacheEnabled;

    public CacheHandler() {
        cacheEnabled = false;

        Map<String, Object> config = Configuration.root().asMap();
        try {
            cacheEnabled = (Boolean) ((Map) config.get("memcached")).get("enabled");
        } catch (ClassCastException | NullPointerException ignored) {
        }

        logger.info(String.format("Memcached set to %s", cacheEnabled));
    }

    @Around(value = "execution(play.mvc.Result controllers.taozi..*(..))" +
            "&&@annotation(controllers.aspectj.RemoveOcsCache)")
    public Result removeCache(ProceedingJoinPoint pjp) throws Throwable {
        if (cacheEnabled) {
            MethodSignature ms = (MethodSignature) pjp.getSignature();
            Method method = ms.getMethod();
            RemoveOcsCache annotation = method.getAnnotation(RemoveOcsCache.class);
            String[] keyList = annotation.keyList().split(SEPARATOR);
            for (String keyEntry : keyList) {
                String key = getCacheKey(keyEntry, method.getParameterAnnotations(), pjp.getArgs());
                logger.debug("Remove key: " + key);
                Cache.remove(key);
            }
        }

        return (Result) pjp.proceed();
    }

    @Around(value = "execution(* controllers.taozi..*(..)) && @annotation(controllers.aspectj.UsingOcsCache)")
    public Object tryUsingCache(ProceedingJoinPoint pjp) throws Throwable {
        logger = LogFactory.getLog(this.getClass());
        logger.info(String.format("TRY CACHE: %d", new Random().nextInt()));

        if (!cacheEnabled) {
            return pjp.proceed();
        }

        Http.Context context = Http.Context.current();

        // 缓存策略：none表示不使用缓存，refresh表示无视现有缓存，强制将其刷新
        String[] tmp = context.request().queryString().get("cachePolicy");
        String cachePolicy = (tmp != null && tmp.length >= 1) ? tmp[0] : "";

        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        UsingOcsCache annotation = method.getAnnotation(UsingOcsCache.class);

        // 获得序列化器名称
        String serializerName = annotation.serializer();

        // 获得默认的serializer
        if (serializerName.equals("")) {
            Class<?> returnType = method.getReturnType();
            if (Result.class.isAssignableFrom(returnType)) {
                // 返回的是WrappedStatus类型，默认采用ResultSerializer
                serializerName = "WrappedResult";
            }else if(String.class.isAssignableFrom(returnType)){
                // 采用StringSerializer
                serializerName = "String";
            }
        }

        // 缓存策略为none，或者没有指定序列化器的名称，则不启用缓存
        if (cachePolicy.equals("none") || serializerName.equals("")) {
            return pjp.proceed();
        }

        String key = getCacheKey(annotation.key(), method.getParameterAnnotations(), pjp.getArgs());

        if (cachePolicy.equals("refresh")) {
            // 强制刷新缓存
            synchronized (key.intern()) {
                return fetchAndRefreshCache(pjp, key, annotation, serializerName);
            }
        }

        // 双重检查锁
        String jsonStr = (String) Cache.get(key);
        if (jsonStr == null || jsonStr.isEmpty()) {
            synchronized (key.intern()) {
                //再次尝试从缓存中获取值
                jsonStr = (String) Cache.get(key);
                if (jsonStr == null || jsonStr.isEmpty()) {
                    return fetchAndRefreshCache(pjp, key, annotation, serializerName);
                }
            }
        }
        logger.info(String.format("Cache hit: %s", key));

        // 调用反序列化器
        SerializeParser s = ParserFactory.getInstance().create(serializerName);
        if (s == null)
            // 默认情况下，返回字符串
            return jsonStr;
        else
            return s.deserialize(jsonStr);
    }

    @SuppressWarnings("unchecked")
    private Object fetchAndRefreshCache(ProceedingJoinPoint pjp, String key, UsingOcsCache annotation,
                                        String serializerName) throws Throwable {
        //若未命中，则代表是第一次访问，从数据库读取
        Object res = pjp.proceed();

        SerializeParser s = ParserFactory.getInstance().create(serializerName);
        if (s != null) {
            String contents = s.serialize(res);
            safeCaching(key, contents, annotation.expireTime());
        }

        return res;
    }

    /**
     * 对cache的key和value进行长度检查，若长度过大，则不缓存
     *
     * @param key
     * @param cacheValue
     * @param expireTime
     */
    private void safeCaching(String key, String cacheValue, int expireTime) {
        if (key.length() > MAX_KEY_LENGTH) {
            logger.warn("Cannot do caching: key size out of limit (" + MAX_KEY_LENGTH + " Bytes)");
        }
        if (cacheValue.length() <= MAX_VALUE_LENGTH) {
            logger.info(String.format("Set to cache: %s", key));
            Cache.set(key, cacheValue, expireTime);
        } else {
            logger.warn("Cannot do caching: data size out of limit (" + MAX_VALUE_LENGTH + " Bytes)");
        }
    }

    /**
     * 获得cache的key
     *
     * @param rawKey 原始的key，或模板
     */
    private String getCacheKey(String rawKey, Annotation[][] parameterAnnotations, Object[] args) {
        String key = rawKey;
        int i = -1;
        for (Annotation[] parameter : parameterAnnotations) {
            i = i + 1;
            for (Annotation annotationi : parameter) {
                if (annotationi != null && annotationi instanceof Key) {
                    String rep = "\\{" + ((Key) annotationi).tag() + "\\}";
                    key = key.replaceFirst(rep, args[i].toString());
                    break;
                }
            }
        }
        return key.length() <= MAX_KEY_LENGTH ? key : key.substring(0, MAX_KEY_LENGTH);
    }
}
