package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;

/**
 * 介绍的类。
 *
 * @author Zephyre
 */
@Embedded
public class Description implements ITravelPiFormatter {
    public String desc;
    public String details;
    public String tips;


    @Override
    public JsonNode toJson() {
        return null;
    }
}
