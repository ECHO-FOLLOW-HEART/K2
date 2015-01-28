package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
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

import static utils.WrappedStatus.WrappedOk;

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
                    String rep = "%" + i;
                    key = key.replaceFirst(rep, args[i].toString());
                    break;
                }
            }
        }
        return key;
    }

    @Around(value = "execution(play.mvc.Result controllers.taozi..*(..))" +
            "&&@annotation(controllers.UsingCache)")
    public Result tryUsingCache(ProceedingJoinPoint pjp, JoinPoint joinPoint) {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        UsingCache annotation = method.getAnnotation(UsingCache.class);
        String key = getCacheKey(annotation, method.getParameterAnnotations(), joinPoint.getArgs());

        String jsonStr = (String) Cache.get(key);
        if (jsonStr != null && !jsonStr.isEmpty()) {
//            return Utils.createResponse(ErrorCode.NORMAL, Json.parse(jsonStr));
            ObjectNode response = Json.newObject();
            response.put("lastModified", System.currentTimeMillis() / 1000);
            response.put("cached", true);
            response.put("result", Json.parse(jsonStr));
            response.put("code", ErrorCode.NORMAL);
            return WrappedOk(response);
        }

        try {
            Result result = (Result) pjp.proceed();
            JsonNode body = ((WrappedStatus) result).getJsonBody();
            if (body.get("code").asInt(ErrorCode.UNKOWN_ERROR) == ErrorCode.NORMAL) {
                Cache.set(key, body.get("result").toString(), annotation.expireTime());
            }
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return Utils.createResponse(ErrorCode.UNKOWN_ERROR, "Unknown Error");
    }
}
