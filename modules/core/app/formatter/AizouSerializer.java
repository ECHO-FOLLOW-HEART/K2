package formatter;

import com.fasterxml.jackson.databind.JsonSerializer;
import models.AizouBaseEntity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Serializer的基类
 * <p/>
 * Created by zephyre on 1/15/15.
 */
public abstract class AizouSerializer<T extends AizouBaseEntity> extends JsonSerializer<T> {
    protected String getString(String val) {
        return (val != null && !val.isEmpty()) ? val : "";
    }

    protected String getTimestamp(Date ts) {
        return (ts != null) ? dateFormat.format(ts) : "";
    }

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }
}
