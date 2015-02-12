import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import play.Application;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;

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
}