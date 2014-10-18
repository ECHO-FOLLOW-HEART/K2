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