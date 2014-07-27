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
    }
}