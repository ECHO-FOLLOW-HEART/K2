package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import play.api.mvc.ResponseHeader;
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
    // date time c-ip cs-method cs-uri sc-status c-apicode bytes

    @AfterReturning(pointcut = "execution(play.mvc.Result controllers.taozi..*(..))", returning = "result")
    public void logAdvice(Result result) {
        if (!(result instanceof WrappedStatus))
            return;

        JsonNode body = ((WrappedStatus) result).getJsonBody();
        if (body == null) {
            body = Json.parse("{}");
        }
        ResponseHeader s = (ResponseHeader) result.toScala().productElement(0);
        int status = s.status();
        int code = body.get("code") == null ? 0 : body.get("code").asInt(ErrorCode.UNKOWN_ERROR);
        String logLine = getLogLine(code, status, body);
        logger.info(logLine);
    }

    private String getLogLine(int code, int responseStatus, JsonNode result) {
        Date now = new Date();
        String version = "-";
        String platform = "-";
        String userid = "-";
        String ip = "-";
        String method = "-";
        String uri = "-";

        Http.Context context = Http.Context.current();

        if (context != null) {
            version = context.request().getHeader("Version") == null ? "-" : context.request().getHeader("Version");
            platform = context.request().getHeader("Platform") == null ? "-" : context.request().getHeader("Platform");
            userid = context.request().getHeader("UserId") == null ? "-" : context.request().getHeader("UserId");
            ip = context.request().remoteAddress();
            method = context.request().method();
            uri = context.request().uri();
        }
        String status = String.format("%d", responseStatus);
        String serverCode = String.format("%d", code);
        String bytes = "0";
        if (result != null) {
            bytes = String.format("%d", result.toString().length());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

        return dateFormat.format(now) + " " + timeFormat.format(now) + " " + version + " " + platform + " " + userid + " " + ip + " " + method + " " + uri + " " + status + " " + serverCode + " " + bytes;
    }
}
