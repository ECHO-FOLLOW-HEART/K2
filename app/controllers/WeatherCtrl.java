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

import java.util.ArrayList;
import java.util.List;

/**
 * @author lxf
 */
public class WeatherCtrl extends Controller {

    public static JsonNode getLocProfile(SimpleRef loc) {
        ObjectNode response = Json.newObject();
        response.put("id", loc.id.toString());
        String enName = loc.enName;
        if (enName != null)
            response.put("enName", enName);
        String zhName = loc.zhName;
        if (zhName != null)
            response.put("zhName", zhName);
        return response;
    }

    public static JsonNode getWeatherProfile(WeatherItem weather) {
        ObjectNode response = Json.newObject();
        response.put("code", weather.code);
        Double currentTemperature = weather.currTemperature;
        Double lowerTemperature = weather.lowerTemperature;
        Double upperTemperature = weather.upperTemperature;
        if (currentTemperature != null)
            response.put("currentTemperature", currentTemperature);
        if (lowerTemperature != null)
            response.put("lowerTemperature", lowerTemperature);
        if (upperTemperature != null)
            response.put("upperTemperature", upperTemperature);
        return response;
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

        }
        response.put("updateTime",weather.updateTime.toString());
        return Utils.createResponse(ErrorCode.NORMAL, response);
    }
}
