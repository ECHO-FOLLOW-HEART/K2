package controllers.taozi;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import play.mvc.Http;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Heaven on 2015/1/10.
 */
@Aspect
public class AccessLogAsp {
    private static final String ACCESS = "access";
    private Log logger = LogFactory.getLog(ACCESS);
    // date time c-ip cs-method cs-uri sc-status bytes cached
    private static final String FORMAT = "%d %t %ip %m %u %s %b %c";

    @Before("args(code, result) " +
            "&& call(play.mvc.Result createResponse(int, com.fasterxml.jackson.databind.JsonNode))" +
            "&& cflow(call(* controllers.taozi..*(..)))")
    public void logNormalState(int code, JsonNode result) {
        Http.Context context = Http.Context.current();
        String logLine = getLogLine(context, code, result);
        logger.info(logLine);
    }

    @AfterThrowing(value = "call(play.mvc.Result controllers.taozi..*(..))", throwing = "throwing")
    public void logUnkownState(Throwable throwing) {
        Http.Context context = Http.Context.current();
        String LogLine = getLogLine(context, ErrorCode.UNKOWN_ERROR, null);
        logger.info(LogLine);
    }

    private String getLogLine(Http.Context context, int code, JsonNode result) {
        String ret = FORMAT;
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
        if (result != null) {
            bytes = String.format("%d", result.toString().length());
        }
        //TODO 如何知道是否使用了 cached？
        String cached = "0";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        ret = ret.replaceFirst("%d", dateFormat.format(now));
        ret = ret.replaceFirst("%t", timeFormat.format(now));
        ret = ret.replaceFirst("%ip", ip);
        ret = ret.replaceFirst("%m", method);
        ret = ret.replaceFirst("%u", uri);
        ret = ret.replaceFirst("%s", status);
        ret = ret.replaceFirst("%b", bytes);
        ret = ret.replaceFirst("%c", cached);
        return ret;
    }
}
