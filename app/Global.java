import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import play.Application;
import play.GlobalSettings;
import play.api.libs.concurrent.Promise;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;
import java.util.TimeZone;

import static play.mvc.Results.ok;

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
        try {
            if (request.getQueryString("v") != null) {
                String[] verStr = request.getQueryString("v").split("\\.");
                int ver = Integer.valueOf(verStr[0]) * 10000 + Integer.valueOf(verStr[1]) * 100 + Integer.valueOf(verStr[2]);
                request.headers().put("verIndex", new String[]{String.valueOf(ver)});
            }
//            JsonNode body = request.body().asJson();
            String macKey = request.getHeader("macKey");
            String macData = request.getHeader("macData");
            Log logger = LogFactory.getLog("com.travelpi");
            if (!macKey.isEmpty() && !macData.isEmpty()) {
                logger.info("##hex:"+Crypto.HmacSHA256(macKey, macData));
                System.out.println("##hex:"+Crypto.HmacSHA256(macKey, macData));
            } else {
                System.out.println("##empty!");
                logger.info("##empty!");
            }
        } catch (Exception e) {
        }
        return super.onRequest(request, actionMethod);
    }

}