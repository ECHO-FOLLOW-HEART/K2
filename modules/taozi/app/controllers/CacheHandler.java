package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;
import utils.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static play.mvc.Results.ok;

/**
 * Created by Heaven on 2015/1/22.
 */
@Aspect
public class CacheHandler {
    Log logger = LogFactory.getLog("CacheHandler");

    @Before(value = "args(code, result) " +
            "&& call(play.mvc.Result createResponse(int, com.fasterxml.jackson.databind.JsonNode))" +
            "&& cflow(call(* controllers.taozi..*(..)))")
    public void doCaching(int code, JsonNode result, JoinPoint joinPoint, JoinPoint.EnclosingStaticPart enclosing) {
        MethodSignature ms = (MethodSignature) enclosing.getSignature();
        Method method = ms.getMethod();
        UsingCache annotation = method.getAnnotation(UsingCache.class);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (annotation == null) {
            return;
        }

        String key = getCacheKey(annotation, parameterAnnotations, joinPoint.getArgs());

        int timeout = annotation.exprieTime();

        Cache.set(key, result.toString(), timeout);
        logger.info("cache result : key = " + key + " timeout = " + timeout + "s");
    }

    private String getCacheKey(UsingCache annotation, Annotation[][] parameterAnnotations, Object[] args) {
        String key = annotation.key();
        int i = -1;
        for (Annotation[] parameter : parameterAnnotations) {
            i = i + 1;
            logger.info(args[i].toString());
            for (Annotation annotationi : parameter) {
                if (annotationi != null && annotationi instanceof CacheKey) {
                    String rep = "${" + i + "}";
                    key = key.replaceFirst(rep, args[i].toString());
                    break;
                }
            }
        }
        return key;
    }

    @Around("call(play.mvc.Result controllers.taozi..*(..))" +
            "&&@annotation(UsingCache)" +
            "&&!within(CacheHandler)")
    public Result tryUsingCache(ProceedingJoinPoint pjp, JoinPoint joinPoint) {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        UsingCache annotation = method.getAnnotation(UsingCache.class);
//        String key = annotation.key();
        String key = getCacheKey(annotation, method.getParameterAnnotations(), joinPoint.getArgs());

        String jsonStr = (String) Cache.get(key);
        if (jsonStr != null && !jsonStr.isEmpty()) {

            //The code below is almost the same with Utils.createResponse(errCode, result)
            //but it throw IllegalAccessException when i use this function
            //i don't know why ...
            ObjectNode response = Json.newObject();
            response.put("lastModified", System.currentTimeMillis() / 1000);
            response.put("result", Json.parse(jsonStr));
            response.put("code", ErrorCode.NORMAL + 1);     //ErrorCode.NORMAL+1 means hitting cache
            return ok(response);
        }

        try {
            return (Result) pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return Utils.createResponse(ErrorCode.UNKOWN_ERROR, "Unknown Error");
    }
}
