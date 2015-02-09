package formatter.base;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * Created by zephyre on 2/8/15.
 */
public interface AizouObjectWriter<T> {
    public void write(T item, JsonGenerator jgen) throws IOException;
}
