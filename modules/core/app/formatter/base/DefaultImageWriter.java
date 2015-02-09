package formatter.base;

import com.fasterxml.jackson.core.JsonGenerator;
import models.misc.ImageItem;

import java.io.IOException;

/**
 * Created by zephyre on 2/8/15.
 */
public class DefaultImageWriter implements ImageWriter {
    @Override
    public void write(ImageItem item, JsonGenerator jgen) throws IOException {

    }
}
