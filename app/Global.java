import exception.AizouException;
import exception.ErrorCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import utils.Utils;

import java.lang.reflect.Method;
import java.util.TimeZone;

public class Global extends GlobalSettings {
    @SuppressWarnings("unchecked")
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{GzipFilter.class};
    }

    private boolean debugLevel;

    @Override
    public void onStart(Application app) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        Log logger = LogFactory.getLog("com.travelpi");
        logger.info("Application started");
        logger.info("北京爱走天下网络科技有限责任公司");

        String runLevel = Configuration.root().getString("runlevel");
        debugLevel = (runLevel != null && runLevel.toLowerCase().equals("debug"));
    }

    @Override
    public F.Promise<Result> onError(Http.RequestHeader requestHeader, Throwable throwable) {
        final Throwable ex = throwable;

        return F.Promise.promise(new F.Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                if (ex instanceof AizouException) {
                    AizouException aizouException = (AizouException) ex;
                    if (debugLevel)
                        return Utils.createResponse(aizouException.getErrCode(), aizouException.getMessage());
                    else
                        return Utils.createResponse(aizouException.getErrCode(), "");
                } else
                    return Utils.createResponse(ErrorCode.UNKOWN_ERROR);
            }
        });
    }

    public Action onRequest(Http.Request request, Method actionMethod) {
        try {
            if (request.getQueryString("v") != null) {
                String[] verStr = request.getQueryString("v").split("\\.");
                int ver = Integer.valueOf(verStr[0]) * 10000 + Integer.valueOf(verStr[1]) * 100 + Integer.valueOf(verStr[2]);
                request.headers().put("verIndex", new String[]{String.valueOf(ver)});
            }
        } catch (Exception e) {
        }
        return super.onRequest(request, actionMethod);
    }
}