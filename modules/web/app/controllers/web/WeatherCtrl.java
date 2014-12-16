package controllers.web;


import aizou.core.WeatherAPI;
import exception.AizouException;
import exception.ErrorCode;
import models.misc.YahooWeather;
import org.bson.types.ObjectId;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

/**
 * @author lxf
 */
public class WeatherCtrl extends Controller {

    public static Result getWeatherDetail(String cityId) throws AizouException {
        ObjectId cityOid;
        try {
            cityOid = new ObjectId(cityId);
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality id: %s", cityId));
        }
        YahooWeather yahooWeather = WeatherAPI.weatherDetails(cityOid);
        if (yahooWeather == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "");
        return Utils.createResponse(ErrorCode.NORMAL, yahooWeather.toJson());
    }
}
