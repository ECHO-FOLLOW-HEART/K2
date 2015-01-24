package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.UsingCache;
import exception.ErrorCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import play.Logger;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;
import utils.Utils;

import java.lang.reflect.Method;

/**
 * Created by Heaven on 2015/1/22.
 */
@Aspect
public class CacheHandler {

    @Before("args(code, result) " +
            "&& call(play.mvc.Result createResponse(int, com.fasterxml.jackson.databind.JsonNode))" +
            "&& cflow(call(* controllers.taozi..*(..)))")
    public void doCaching(int code, JsonNode result, JoinPoint joinPoint, JoinPoint.EnclosingStaticPart enclosing) {
        Logger.info("before doCaching.." + joinPoint.getSignature());
        Logger.info("caller is : " + enclosing.getSignature());
        MethodSignature ms = (MethodSignature) enclosing.getSignature();
        Method method = ms.getMethod();
        UsingCache annotation = method.getAnnotation(UsingCache.class);
        if (annotation == null) {
            return;
        }
        String key = annotation.key();
        int timeout = annotation.exprieTime();
        Cache.set(key, result.toString(), timeout);
        Logger.info("Cache: " + key + " timeout: " + timeout);
    }

    @Around("call(play.mvc.Result controllers.taozi..*(..))" +
            "&&@annotation(UsingCache)" +
            "&&!within(CacheHandler)")
    public Result tryUsingCache(ProceedingJoinPoint pjp) {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        UsingCache annotation = method.getAnnotation(UsingCache.class);
        String key = annotation.key();

        String jsonStr = (String) Cache.get(key);
        if (jsonStr != null && !jsonStr.isEmpty()) {
            Logger.info("Cache hit!");
            Logger.info(jsonStr);
            return Utils.createResponse(ErrorCode.NORMAL + 1, Json.parse(jsonStr));
        }

        try {
            return (Result) pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return Utils.createResponse(ErrorCode.UNKOWN_ERROR, "Unkown Error");
    }
}
