package controllers.aspectj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import play.api.mvc.ResponseHeader;
import play.mvc.Http;
import play.mvc.Result;
import utils.WrappedStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Heaven on 2015/1/10.
 */
//@Aspect
public class AccessLogger {
    private static final String ACCESS = "access";
    private Log logger = LogFactory.getLog(ACCESS);
    // date time c-ip cs-method cs-uri sc-status c-apicode bytes

    @AfterReturning(pointcut = "execution(play.mvc.Result controllers.taozi..*(..))", returning = "result")
    public void logAdvice(Result result) {
        if (!(result instanceof WrappedStatus))
            return;

        String body = ((WrappedStatus) result).getStringBody();
        if (body == null)
            body = "";

        ResponseHeader s = (ResponseHeader) result.toScala().productElement(0);
        int status = s.status();
        String logLine = getLogLine(status, body);
        logger.info(logLine);
    }

    private String getLogLine(int responseStatus, String result) {
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
        String bytes = "0";
        if (result != null) {
            bytes = String.format("%d", result.length());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

        return dateFormat.format(now) + " " + timeFormat.format(now) + " " + version + " " + platform +
                " " + userid + " " + ip + " " + method + " " + uri + " " + status + " " + bytes;
    }
}
