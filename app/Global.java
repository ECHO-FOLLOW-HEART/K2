import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import play.Application;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;
import java.util.TimeZone;

public class Global extends GlobalSettings {
    @SuppressWarnings("unchecked")
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{GzipFilter.class};
    }

    @Override
    public void onStart(Application app) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        Log logger = LogFactory.getLog("com.travelpi");
        logger.info("Application started");
        logger.info("北京爱走天下网络科技有限责任公司");
    }

    @Override
    public Action onRequest(final Http.Request request, Method actionMethod) {
        Log logger = LogFactory.getLog("crypto.debug");
        try {
            if (request.getQueryString("v") != null) {
                String[] verStr = request.getQueryString("v").split("\\.");
                int ver = Integer.valueOf(verStr[0]) * 10000 + Integer.valueOf(verStr[1]) * 100 + Integer.valueOf(verStr[2]);
                request.headers().put("verIndex", new String[]{String.valueOf(ver)});
            }
            String userId = request.getHeader("UserId");
            if (userId != null && !userId.isEmpty()) {
                String rightSignature = Crypto.getSignature(request, "fake12345678");
                String gettedSignature = request.getHeader("Authorization");
                logger.info("right Signature: "+rightSignature);
                logger.info("get   Signature: "+gettedSignature);
                if (!rightSignature.equals(gettedSignature)) {
                    logger.fatal("Signature is invalid - rejected");
                } else {
                    logger.info("Signature is valid - accepted");
                }
            } else {
                logger.debug("Normal request - accepted");
            }
        } catch (Exception e) {
        }
        return super.onRequest(request, actionMethod);
    }

}