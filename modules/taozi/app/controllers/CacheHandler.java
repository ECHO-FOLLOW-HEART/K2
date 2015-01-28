package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import play.Logger;
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
        return key;
    }

    @Around(value = "execution(play.mvc.Result controllers.taozi..*(..))" +
            "&&@annotation(controllers.UsingCache)")
    public Result tryUsingCache(ProceedingJoinPoint pjp, JoinPoint joinPoint) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        UsingCache annotation = method.getAnnotation(UsingCache.class);
        String key = getCacheKey(annotation, method.getParameterAnnotations(), joinPoint.getArgs());

        String jsonStr = (String) Cache.get(key);
        if (jsonStr != null && !jsonStr.isEmpty()) {
            Logger.info("Cache hit!");
            return Utils.createResponse(ErrorCode.NORMAL, Json.parse(jsonStr));
        }

        Result result = (Result) pjp.proceed();
        JsonNode body = ((WrappedStatus) result).getJsonBody();
        if (body.get("code").asInt(ErrorCode.UNKOWN_ERROR) == ErrorCode.NORMAL) {
            Cache.set(key, body.get("result").toString(), annotation.expireTime());
        }
        return result;
    }
}
