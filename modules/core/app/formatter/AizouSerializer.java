package formatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import formatter.base.DefaultObjectIdWriter;
import formatter.base.ObjectIdWriter;
import formatter.taozi.ObjectIdSerializer;
import models.AizouBaseEntity;
import models.AizouBaseItem;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Serializer的基类
 * <p/>
 * Created by zephyre on 1/15/15.
 */
public abstract class AizouSerializer<T extends AizouBaseItem> extends JsonSerializer<T> {

    protected String getString(String val) {
        return (val != null && !val.isEmpty()) ? val : "";
    }

    protected String getTimestamp(Date ts) {
        return (ts != null) ? dateFormat.format(ts) : "";
    }

    public AizouSerializer() {
        oidWriter = new DefaultObjectIdWriter();
    }

    private ObjectIdWriter oidWriter;

    /**
     * ObjectId writers
     *
     * @return
     */
    protected ObjectIdWriter getOidWriter() {
        return oidWriter;
    }

    /**
     * 写入ObjectId
     *
     * @param item
     * @param jgen
     * @throws IOException
     */
    protected void writeObjectId(AizouBaseEntity item, JsonGenerator jgen)
            throws IOException {
        oidWriter.write(item.getId(), jgen);
    }

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }
}
