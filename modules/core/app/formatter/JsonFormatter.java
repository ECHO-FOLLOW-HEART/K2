package formatter;

import com.fasterxml.jackson.databind.JsonNode;
import models.TravelPiBaseItem;

/**
 * Created by zephyre on 10/28/14.
 */
public interface JsonFormatter {
    public JsonNode format(TravelPiBaseItem item);
}
