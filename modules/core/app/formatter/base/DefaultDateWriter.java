package formatter.base;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zephyre on 2/8/15.
 */
public class DefaultDateWriter implements DateWriter {
    @Override
    public void write(Date item, JsonGenerator jgen) throws IOException {
        (new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ")).format(item);
    }
}
