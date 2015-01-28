package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.WrappedStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Heaven on 2015/1/10.
 */
@Aspect
public class AccessLogger {
    private static final String ACCESS = "access";
    private Log logger = LogFactory.getLog(ACCESS);
    // date time c-ip cs-method cs-uri sc-status bytes cached

    @Around(value = "execution(play.mvc.Result controllers.taozi..*(..))")
    public Result logAccessment(ProceedingJoinPoint pjp) {
        Http.Context context = Http.Context.current();
        WrappedStatus result = null;
        try {
            result = (WrappedStatus) pjp.proceed();
            JsonNode body = result.getJsonBody();
            if (body == null) {
                body = Json.parse("{}");
            }
            String logLine = getLogLine(context, body.get("code").asInt(ErrorCode.UNKOWN_ERROR), body);
            logger.info(logLine);
        } catch (Throwable throwable) {
            //assert that pjp.proceed() won't throw exception
            throwable.printStackTrace();
        } finally {
            return result;
        }
    }

    private String getLogLine(Http.Context context, int code, JsonNode result) {
        Date now = new Date();
        String ip = "-";
        String method = "-";
        String uri = "-";
        if (context != null) {
            ip = context.request().remoteAddress();
            method = context.request().method();
            uri = context.request().uri();
        }
        String status = String.format("%d", code);
        String bytes = "0";
        String cached = "0";
        if (result != null) {
            bytes = String.format("%d", result.toString().length());
            if (result.get("cached") != null && result.get("cached").asBoolean(false)) {
                cached = "1";
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

        return dateFormat.format(now) + " " + timeFormat.format(now) + " " + ip + " " + method + " " + uri + " " + status + " " + bytes + " " + cached + " ";
    }
}
