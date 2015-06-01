package controllers.aspectj;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import play.api.mvc.ResponseHeader;
import play.mvc.Http;
import play.mvc.Result;
import utils.WrappedStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Heaven on 2015/1/10.
 */
@Aspect
public class AccessLogger {
    private static final String ACCESS = "access";
    private Log logger = LogFactory.getLog(ACCESS);

    @Around(value = "execution(public play.mvc.Result controllers.app..*(..))")
    public Result logAspect(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Result r = (Result) pjp.proceed();
        long end = System.currentTimeMillis();

        logAdvice(r, end - start);
        return r;
    }

    public void logAdvice(Result result, long timeCost) {
        String body = "";
        if (result instanceof WrappedStatus) {
            body = ((WrappedStatus) result).getStringBody();
            if (body == null)
                body = "";
        }

        ResponseHeader s = (ResponseHeader) result.toScala().productElement(0);
        int status = s.status();
        String logLine = getLogLine(status, body, timeCost);
        logger.info(logLine);
    }

    private String getHeaderEntry(Map<String, String[]> headers, String key) {
        String[] val = headers.get(key);
        if (val == null || val.length == 0)
            return "-";
        else
            return StringUtils.join(val, ";");
    }

    private String getLogLine(int responseStatus, String result, long timeCost) {
//        date time c-version c-platform c-userid c-ip cs-method cs-uri sc-status s-code bytes timeCost

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date now = new Date();
        String date = dateFormat.format(now);
        String time = timeFormat.format(now);


        Http.Context context = Http.Context.current();
        Map<String, String[]> headers = context.request().headers();
        String platform = getHeaderEntry(headers, "Platform");
        String version = getHeaderEntry(headers, "Version");
        String userId = getHeaderEntry(headers, "UserId");

        String ip = context.request().remoteAddress();
        String method = context.request().method();
        String uri = context.request().uri();

        int bytes = 0;
        if (result != null) {
            bytes = result.length();
        }

        Pattern pattern = Pattern.compile("\"code\":(\\d+)");

        String code = "-";
        if (result != null) {
            Matcher m = pattern.matcher(result);
            if (m.find())
                code = m.group(1);
        }

        return String.format("%s %s %s %s %s %s %s %s %d %s %d %d", date, time, version, platform, userId, ip,
                method, uri, responseStatus, code, bytes, timeCost);
    }
}
