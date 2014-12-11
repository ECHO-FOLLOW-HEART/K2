package formatter;

import com.fasterxml.jackson.databind.JsonNode;
import models.AizouBaseEntity;

/**
 * Created by zephyre on 10/28/14.
 */
public interface JsonFormatter {
    public JsonNode format(AizouBaseEntity item);
}
