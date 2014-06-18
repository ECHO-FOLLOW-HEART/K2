package controllers;

import models.geos.*;
import play.db.ebean.Model;
import play.db.ebean.Transactional;
import play.mvc.*;

import utils.Utils;
import views.html.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    @Transactional
    public static Result feedData() {
        HashMap<String, Object> kvPair = new HashMap<String, Object>() {
            {
                put("enContinentName", "ASIA");
                put("zhContinentName", "亚洲");
            }
        };
        Continent cont = (Continent) Utils.create(Continent.class, kvPair);
        cont.save();

        kvPair = new HashMap<String, Object>() {
            {
                put("enContinentName", "NORTH AMERICA");
                put("zhContinentName", "北美洲");
            }
        };
        cont = (Continent) Utils.create(Continent.class, kvPair);
        cont.save();

        kvPair = new HashMap<String, Object>() {
            {
                put("enContinentName", "EUROPE");
                put("zhContinentName", "欧洲");
            }
        };
        cont = (Continent) Utils.create(Continent.class, kvPair);
        cont.save();

        kvPair = new HashMap<String, Object>() {
            {
                put("enSubContinentName", "SOUTH ASIA");
                put("zhSubContinentName", "南亚");
            }
        };
        SubContinent subCont = (SubContinent) Utils.create(SubContinent.class, kvPair);
        subCont.save();
        final SubContinent finalSubCont = subCont;

        final Continent finalCont = (new Model.Finder<Long, Continent>(Long.class, Continent.class)).where().eq("enContinentName", "ASIA").findUnique();

        kvPair = new HashMap<String, Object>() {
            {
                put("countryCode2", "MY");
                put("enCountryName", "MALAYSIA");
                put("zhCountryName", "马来西亚");
                put("continent", finalCont);
                put("subContinent", finalSubCont);
            }
        };
        Country country = (Country) Utils.create(Country.class, kvPair);
        country.save();

        kvPair = new HashMap<String, Object>() {
            {
                put("enSubContinentName", "EAST ASIA");
                put("zhSubContinentName", "东亚");
            }
        };
        subCont = (SubContinent) Utils.create(SubContinent.class, kvPair);
        subCont.save();
        final SubContinent finalSubCont1 = subCont;

        kvPair = new HashMap<String, Object>() {
            {
                put("countryCode2", "CN");
                put("enCountryName", "CHINA");
                put("zhCountryName", "中国");
                put("continent", finalCont);
                put("subContinent", finalSubCont1);
            }
        };
        country = (Country) Utils.create(Country.class, kvPair);
        country.save();

        CityTag cityTag = new CityTag();
        cityTag.tagName = "古镇风情";
        cityTag.save();
        cityTag = new CityTag();
        cityTag.tagName = "六朝古都";
        cityTag.save();
        cityTag = new CityTag();
        cityTag.tagName = "美食之都";
        cityTag.save();
        cityTag = new CityTag();
        cityTag.tagName = "高原风光";
        cityTag.save();

        kvPair = new HashMap<String, Object>() {
            {
                put("country", Country.finder.byId("CN"));
                put("enCityName", "BEIJING");
                put("zhCityName", "北京");
                put("lat", 40f);
                put("lng", 120f);
            }
        };
        City city = (City) Utils.create(City.class, kvPair);
        city.tagList = new ArrayList<CityTag>() {
            {
                add(CityTag.finder.where().eq("tagName", "六朝古都").findUnique());
                add(CityTag.finder.where().eq("tagName", "美食之都").findUnique());
            }
        };
        city.save();

        kvPair = new HashMap<String, Object>() {
            {
                put("country", Country.finder.byId("CN"));
                put("enCityName", "CHENGDU");
                put("zhCityName", "成都");
                put("lat", 31f);
                put("lng", 108f);
            }
        };
        city = (City) Utils.create(City.class, kvPair);
        city.save();
        city = City.finder.where().eq("enCityName", "CHENGDU").findUnique();
        city.tagList = new ArrayList<CityTag>() {
            {
                add(CityTag.finder.where().eq("tagName", "高原风光").findUnique());
                add(CityTag.finder.where().eq("tagName", "美食之都").findUnique());
            }
        };
        city.update();


        return ok("SUCCESS");
    }

    public static Result feedTag() {
        ArrayList<CityTag> tagList = new ArrayList<CityTag>() {
            {
                add(CityTag.finder.where().eq("tagName", "六朝古都").findUnique());
                add(CityTag.finder.where().eq("tagName", "美食之都").findUnique());
            }
        };
        City city = City.finder.where().eq("enCityName", "BEIJING").findUnique();
        city.tagList = tagList;
        city.update();

        tagList = new ArrayList<CityTag>() {
            {
                add(CityTag.finder.where().eq("tagName", "高原风光").findUnique());
                add(CityTag.finder.where().eq("tagName", "美食之都").findUnique());
            }
        };
        city = City.finder.where().eq("enCityName", "CHENGDU").findUnique();
        city.tagList = tagList;
        city.update();
        return ok("SUCCESS");
    }
}
