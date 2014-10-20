package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.WeatherAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.misc.SimpleRef;
import models.morphia.misc.Weather;
import models.morphia.misc.WeatherItem;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import java.util.List;

/**
 * @author lxf
 */
public class WeatherCtrl extends Controller {

    public static JsonNode getLocProfile(SimpleRef loc) {
        ObjectNode locNode = Json.newObject();
        locNode.put("id", loc.id.toString());
        String enName = loc.enName;
        if (enName != null)
            locNode.put("enName", enName);
        String zhName = loc.zhName;
        if (zhName != null)
            locNode.put("zhName", zhName);
        return locNode;
    }

    public static JsonNode getWeatherProfile(WeatherItem weatheritem) {
        ObjectNode currNode = Json.newObject();
        currNode.put("code", weatheritem.code);
        Double currentTemperature = weatheritem.currTemperature;
        Double lowerTemperature = weatheritem.lowerTemperature;
        Double upperTemperature = weatheritem.upperTemperature;
        if (currentTemperature != null)
            currNode.put("currentTemperature", currentTemperature);
        if (lowerTemperature != null)
            currNode.put("lowerTemperature", lowerTemperature);
        if (upperTemperature != null)
            currNode.put("upperTemperature", upperTemperature);
        return currNode;
    }

    public static Result getWeatherDetail(long id) throws TravelPiException {
        Weather weather = WeatherAPI.weatherDetails(new ObjectId(id + ""));
        ObjectNode response = Json.newObject();
        ObjectNode locNode = (ObjectNode) getLocProfile(weather.loc);
        response.put("loc",locNode);
        ObjectNode currweatherNode = (ObjectNode) getWeatherProfile(weather.current);
        response.put("current",currweatherNode);
        List<WeatherItem> weathernodelist = weather.forecast;

        for (WeatherItem weatheritem:weathernodelist) {
            ObjectNode node = (ObjectNode) getWeatherProfile(weatheritem);
            response.put("forecast",node);
        }
        response.put("updateTime",weather.updateTime.toString());
        return Utils.createResponse(ErrorCode.NORMAL, response);
    }
}
