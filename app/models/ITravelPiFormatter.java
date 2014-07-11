package models;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 所有TravelPid数据的基类接口
 *
 * @author Zephyre
 */
public interface ITravelPiFormatter {
    public JsonNode toJson();
}
