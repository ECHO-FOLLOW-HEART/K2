package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.geos.*;
import models.tag.LocalityTag;
import play.db.ebean.Model;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.*;

import utils.DataImporter;
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

        LocalityTag cityTag = new LocalityTag();
        cityTag.cityTagName = "古镇风情";
        cityTag.save();
        cityTag = new LocalityTag();
        cityTag.cityTagName = "六朝古都";
        cityTag.save();
        cityTag = new LocalityTag();
        cityTag.cityTagName = "美食之都";
        cityTag.save();
        cityTag = new LocalityTag();
        cityTag.cityTagName = "高原风光";
        cityTag.save();

        kvPair = new HashMap<String, Object>() {
            {
                put("country", Country.finder.byId("CN"));
                put("enLocalityName", "BEIJING");
                put("zhLocalityName", "北京");
                put("lat", 40f);
                put("lng", 120f);
            }
        };
        Locality locality = (Locality) Utils.create(Locality.class, kvPair);
        locality.tagList = new ArrayList<LocalityTag>() {
            {
                add(LocalityTag.finder.where().eq("cityTagName", "六朝古都").findUnique());
                add(LocalityTag.finder.where().eq("cityTagName", "美食之都").findUnique());
            }
        };
        locality.save();

        kvPair = new HashMap<String, Object>() {
            {
                put("country", Country.finder.byId("CN"));
                put("enLocalityName", "CHENGDU");
                put("zhLocalityName", "成都");
                put("lat", 31f);
                put("lng", 108f);
            }
        };
        locality = (Locality) Utils.create(Locality.class, kvPair);
        locality.save();
        locality = Locality.finder.where().eq("enLocalityName", "CHENGDU").findUnique();
        locality.tagList = new ArrayList<LocalityTag>() {
            {
                add(LocalityTag.finder.where().eq("cityTagName", "高原风光").findUnique());
                add(LocalityTag.finder.where().eq("cityTagName", "美食之都").findUnique());
            }
        };
        locality.update();


        return ok("SUCCESS");
    }

    @Transactional
    public static Result feedTag() {
        ArrayList<LocalityTag> tagList = new ArrayList<LocalityTag>() {
            {
                add(LocalityTag.finder.where().eq("cityTagName", "六朝古都").findUnique());
                add(LocalityTag.finder.where().eq("cityTagName", "美食之都").findUnique());
            }
        };
        Locality locality = Locality.finder.where().eq("enLocalityName", "BEIJING").findUnique();
        locality.tagList = tagList;
        locality.update();

        tagList = new ArrayList<LocalityTag>() {
            {
                add(LocalityTag.finder.where().eq("cityTagName", "高原风光").findUnique());
                add(LocalityTag.finder.where().eq("cityTagName", "美食之都").findUnique());
            }
        };
        locality = Locality.finder.where().eq("enLocalityName", "CHENGDU").findUnique();
        locality.tagList = tagList;
        locality.update();
        return ok("SUCCESS");
    }

    @Transactional
    public static Result test() {
        return Results.TODO;
    }

    @Transactional
    public static Result geoImport(int start, int count) {
        DataImporter importer = new DataImporter("localhost", 3306, "vxp", "vxp123", "vxp_raw");
        final JsonNode nodeProvince = importer.importGeoSite(start, count);
        JsonNode node = Json.toJson(new HashMap<String, Object>() {
            {
                put("province", nodeProvince);
            }
        });
        return ok(node);
    }

    @Transactional
    public static Result trainImport(int start, int count) {
        DataImporter importer = new DataImporter("localhost", 3306, "vxp", "vxp123", "vxp_raw");
//        final JsonNode nodeStation = importer.importTrainSite(start, count);
//        final JsonNode nodeRoute = importer.importTrainRoute(start, count);
        final JsonNode nodeSchedule = importer.importTrainTimetable(start, count);
        JsonNode node = Json.toJson(new HashMap<String, Object>() {
            {
//                put("station", nodeStation);
//                put("route", nodeRoute);
                put("schedule", nodeSchedule);
            }
        });
        return Utils.createResponse(ErrorCode.NORMAL, node);
    }
}
