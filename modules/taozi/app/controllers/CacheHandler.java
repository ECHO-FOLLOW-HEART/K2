package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;
import utils.Utils;
import utils.WrappedStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by Heaven on 2015/1/22.
 */
@Aspect
public class CacheHandler {
    public static int MAX_KEY_LENGTH = 1000;        //1KB
    public static int MAX_VALUE_LENGTH = 1000000;   //1MB
    Log logger = LogFactory.getLog(this.getClass());

    @Around(value = "execution(play.mvc.Result controllers.taozi..*(..))" +
            "&&@annotation(controllers.UsingCache)")
    public Result tryUsingCache(ProceedingJoinPoint pjp, JoinPoint joinPoint) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        UsingCache annotation = method.getAnnotation(UsingCache.class);
        String key = getCacheKey(annotation, method.getParameterAnnotations(), joinPoint.getArgs());
        String jsonStr = (String) Cache.get(key);

        //缓存命中
        if (jsonStr != null && !jsonStr.isEmpty()) {
            logger.info("cache hit!");
            return Utils.createResponse(ErrorCode.NORMAL, Json.parse(jsonStr));
        }

        //缓存未命中
        synchronized (key.intern()) {
            return getResultFromDB(pjp, key, annotation);
        }
    }

    private Result getResultFromDB(ProceedingJoinPoint pjp, String key, UsingCache annotation) throws Throwable {
        //再次尝试从缓存中获取值
        String jsonStr = (String) Cache.get(key);
        if (jsonStr != null && !jsonStr.isEmpty()) {
            logger.info("cache hit!");
            return Utils.createResponse(ErrorCode.NORMAL, Json.parse(jsonStr));
        }

        //若未命中，则代表是第一次访问，从数据库读取
        Result result = (Result) pjp.proceed();
        JsonNode body = ((WrappedStatus) result).getJsonBody();
        if (body.get("code").asInt(ErrorCode.UNKOWN_ERROR) == ErrorCode.NORMAL) {
            String cacheValue = body.get("result").toString();
            if (cacheValue.length() <= MAX_VALUE_LENGTH) {
                Cache.set(key, cacheValue, annotation.expireTime());
            } else {
                logger.info("Cannot do caching: data size out of limit ("+MAX_VALUE_LENGTH+" Bytes)");
            }
        }
        return result;
    }

    private String getCacheKey(UsingCache annotation, Annotation[][] parameterAnnotations, Object[] args) {
        String key = annotation.key();
        int i = -1;
        for (Annotation[] parameter : parameterAnnotations) {
            i = i + 1;
            for (Annotation annotationi : parameter) {
                if (annotationi != null && annotationi instanceof CacheKey) {
                    String rep = "\\{" + ((CacheKey) annotationi).tag() + "\\}";
                    key = key.replaceFirst(rep, args[i].toString());
                    break;
                }
            }
        }
        return key.length() <= MAX_KEY_LENGTH ?key:key.substring(0, MAX_KEY_LENGTH);
    }
}
