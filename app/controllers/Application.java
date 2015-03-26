package controllers;

import exception.AizouException;
import org.json.JSONException;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;

public class Application extends Controller {

    public static Result index() throws AizouException, IOException, JSONException {
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
//        Connection connection = factory.newConnection();
//        Channel channel = connection.createChannel();
//
//        channel.exchangeDeclare("tasks", "topic", true);
//
//        JSONObject json = new JSONObject();
////        json.put("expires", (Object) null);
////        json.put("utc", (Object) null);
//        json.put("args", new JSONArray().put(12).put(10));
////        json.put("chord", (Object) null);
////        json.put("callbacks", (Object) null);
////        json.put("errbacks", (Object) null);
////        json.put("taskset", (Object) null);
//        json.put("id", "test.add.12-10");
////        json.put("retries", 0);
//        json.put("task", "tasks.add");
////        json.put("timelimit", new JSONArray().put((Object) null).put((Object) null));
////        json.put("eta", (Object) null);
////        json.put("kwargs", new JSONObject());
//
//        channel.basicPublish("tasks", "task.add",
//                new AMQP.BasicProperties.Builder().contentEncoding("utf-8").contentType("application/json").build(), json.toString().getBytes());
//
//        Logger.info("[x] " + json.toString());
//
//        channel.close();
//        connection.close();

        return ok("Hello World");
    }

    public static Result set() {
        String get = (String) Cache.get("key123");
        if (get == null || get.isEmpty()) {
            get = "data";
            Cache.set("key123", get, 10);
            return ok("get data");
        }
        return ok("get data from cache");
    }

//    @Transactional
//    public static Result feedData() {
//        HashMap<String, Object> kvPair = new HashMap<String, Object>() {
//            {
//                put("enContinentName", "ASIA");
//                put("zhContinentName", "亚洲");
//            }
//        };
//        Continent cont = (Continent) Utils.create(Continent.class, kvPair);
//        cont.save();
//
//        kvPair = new HashMap<String, Object>() {
//            {
//                put("enContinentName", "NORTH AMERICA");
//                put("zhContinentName", "北美洲");
//            }
//        };
//        cont = (Continent) Utils.create(Continent.class, kvPair);
//        cont.save();
//
//        kvPair = new HashMap<String, Object>() {
//            {
//                put("enContinentName", "EUROPE");
//                put("zhContinentName", "欧洲");
//            }
//        };
//        cont = (Continent) Utils.create(Continent.class, kvPair);
//        cont.save();
//
//        kvPair = new HashMap<String, Object>() {
//            {
//                put("enSubContinentName", "SOUTH ASIA");
//                put("zhSubContinentName", "南亚");
//            }
//        };
//        SubContinent subCont = (SubContinent) Utils.create(SubContinent.class, kvPair);
//        subCont.save();
//        final SubContinent finalSubCont = subCont;
//
//        final Continent finalCont = (new Model.Finder<Long, Continent>(Long.class, Continent.class)).where().eq("enContinentName", "ASIA").findUnique();
//
//        kvPair = new HashMap<String, Object>() {
//            {
//                put("countryCode2", "MY");
//                put("enCountryName", "MALAYSIA");
//                put("zhCountryName", "马来西亚");
//                put("continent", finalCont);
//                put("subContinent", finalSubCont);
//            }
//        };
//        Country countryDetails = (Country) Utils.create(Country.class, kvPair);
//        countryDetails.save();
//
//        kvPair = new HashMap<String, Object>() {
//            {
//                put("enSubContinentName", "EAST ASIA");
//                put("zhSubContinentName", "东亚");
//            }
//        };
//        subCont = (SubContinent) Utils.create(SubContinent.class, kvPair);
//        subCont.save();
//        final SubContinent finalSubCont1 = subCont;
//
//        kvPair = new HashMap<String, Object>() {
//            {
//                put("countryCode2", "CN");
//                put("enCountryName", "CHINA");
//                put("zhCountryName", "中国");
//                put("continent", finalCont);
//                put("subContinent", finalSubCont1);
//            }
//        };
//        countryDetails = (Country) Utils.create(Country.class, kvPair);
//        countryDetails.save();
//
//        LocalityTag cityTag = new LocalityTag();
//        cityTag.cityTagName = "古镇风情";
//        cityTag.save();
//        cityTag = new LocalityTag();
//        cityTag.cityTagName = "六朝古都";
//        cityTag.save();
//        cityTag = new LocalityTag();
//        cityTag.cityTagName = "美食之都";
//        cityTag.save();
//        cityTag = new LocalityTag();
//        cityTag.cityTagName = "高原风光";
//        cityTag.save();
//
//        kvPair = new HashMap<String, Object>() {
//            {
//                put("countryDetails", Country.finder.byId("CN"));
//                put("enLocalityName", "BEIJING");
//                put("zhLocalityName", "北京");
//                put("lat", 40f);
//                put("lng", 120f);
//            }
//        };
//        Locality locality = (Locality) Utils.create(Locality.class, kvPair);
//        locality.tagList = new ArrayList<LocalityTag>() {
//            {
//                add(LocalityTag.finder.where().eq("cityTagName", "六朝古都").findUnique());
//                add(LocalityTag.finder.where().eq("cityTagName", "美食之都").findUnique());
//            }
//        };
//        locality.save();
//
//        kvPair = new HashMap<String, Object>() {
//            {
//                put("countryDetails", Country.finder.byId("CN"));
//                put("enLocalityName", "CHENGDU");
//                put("zhLocalityName", "成都");
//                put("lat", 31f);
//                put("lng", 108f);
//            }
//        };
//        locality = (Locality) Utils.create(Locality.class, kvPair);
//        locality.save();
//        locality = Locality.finder.where().eq("enLocalityName", "CHENGDU").findUnique();
//        locality.tagList = new ArrayList<LocalityTag>() {
//            {
//                add(LocalityTag.finder.where().eq("cityTagName", "高原风光").findUnique());
//                add(LocalityTag.finder.where().eq("cityTagName", "美食之都").findUnique());
//            }
//        };
//        locality.update();
//
//
//        return ok("SUCCESS");
//    }
//
//    @Transactional
//    public static Result feedTag() {
//        ArrayList<LocalityTag> tagList = new ArrayList<LocalityTag>() {
//            {
//                add(LocalityTag.finder.where().eq("cityTagName", "六朝古都").findUnique());
//                add(LocalityTag.finder.where().eq("cityTagName", "美食之都").findUnique());
//            }
//        };
//        Locality locality = Locality.finder.where().eq("enLocalityName", "BEIJING").findUnique();
//        locality.tagList = tagList;
//        locality.update();
//
//        tagList = new ArrayList<LocalityTag>() {
//            {
//                add(LocalityTag.finder.where().eq("cityTagName", "高原风光").findUnique());
//                add(LocalityTag.finder.where().eq("cityTagName", "美食之都").findUnique());
//            }
//        };
//        locality = Locality.finder.where().eq("enLocalityName", "CHENGDU").findUnique();
//        locality.tagList = tagList;
//        locality.update();
//        return ok("SUCCESS");
//    }
//
//    @Transactional
//    public static Result test() throws TravelPiException {
////        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
////
////        for (models.morphia.geo.Locality loc : ds.createQuery(models.morphia.geo.Locality.class)) {
////            boolean updated = false;
////            models.morphia.geo.Locality parent = loc.parent;
////            if (parent != null) {
////                SimpleRef ref = new SimpleRef();
////                ref.id = parent.id;
////                ref.enName = parent.enName;
////                ref.zhName = parent.zhName;
////                loc.superAdm = ref;
////                updated = true;
////            }
////            models.morphia.geo.Country countryDetails = loc.countryDetails;
////            if (countryDetails != null) {
////                loc.countryId = countryDetails.code;
////                loc.countryEnName = countryDetails.enName;
////                loc.countryZhName = countryDetails.zhName;
////                updated = true;
////            }
////            if (updated)
////                ds.save(loc);
////        }
////
////        // 处理siblings
////        for (int level:new int[]{2,3}) {
////            for (models.morphia.geo.Locality loc:
////                 ds.createQuery(models.morphia.geo.Locality.class).field("level").equal(level)) {
////
//////                for (sib: ds.createQuery(models.morphia.geo.Locality.class).field("superAdmin"))
////            }
////        }
//
//        return Results.ok();
//    }
//
//    @Transactional
//    public static Result geoImport(int start, int count) {
//        DataImporter importer = new DataImporter("localhost", 3306, "vxp", "vxp123", "vxp_raw");
//        final JsonNode nodeProvince = importer.importGeoSite(start, count);
//        JsonNode node = Json.toJson(new HashMap<String, Object>() {
//            {
//                put("province", nodeProvince);
//            }
//        });
//        return ok(node);
//    }
//
//    @Transactional
//    public static Result trainImport(int start, int count) {
//        DataImporter importer = new DataImporter("localhost", 3306, "vxp", "vxp123", "vxp_raw");
//        final JsonNode nodeStation = importer.importTrainSite(start, count);
//        final JsonNode nodeRoute = importer.importTrainRoute(start, count);
//        final JsonNode nodeSchedule = importer.importTrainTimetable(start, count);
//        JsonNode node = Json.toJson(new HashMap<String, Object>() {
//            {
//                put("station", nodeStation);
//                put("route", nodeRoute);
//                put("schedule", nodeSchedule);
//            }
//        });
//        return Utils.createResponse(ErrorCode.NORMAL, node);
//    }
//
//    @Transactional
//    public static Result airImport(int start, int count) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
//        DataImporter importer = DataImporter.init("localhost", 3306, "vxp", "vxp123", "vxp_raw");
//        importer.connect();
////        final JsonNode nodePort = importer.importAirport(start, count);
//        final JsonNode nodeRoute = importer.importAirRoutes(start, count);
////        final JsonNode nodeRoute = importer.importTrainRoute(start, count);
////        final JsonNode nodeSchedule = importer.importTrainTimetable(start, count);
//        JsonNode node = Json.toJson(new HashMap<String, Object>() {
//            {
////                put("station", nodePort);
//                put("route", nodeRoute);
////                put("schedule", nodeSchedule);
//            }
//        });
//        importer.close();
//        return Utils.createResponse(ErrorCode.NORMAL, node);
//    }
//
//    public static Result countryImport() throws TravelPiException {
//        Morphia morphia = Utils.getMorphia();
//        Datastore ds = Utils.getDatastore();
//        morphia.map(models.morphia.geo.Country.class);
//
//        DBCollection col = Utils.getMongoClient().getDB("geo").getCollection("countryDetails");
//
//        DBCursor cursor = col.find(QueryBuilder.start().get());
//        while (cursor.hasNext()) {
//            DBObject loc = cursor.next();
//
//            models.morphia.geo.Country countryDetails = new models.morphia.geo.Country();
//            countryDetails.code = loc.get("_id").toString();
//            Object tmp = loc.get("code3");
//            if (tmp != null)
//                countryDetails.code3 = tmp.toString();
//            tmp = loc.get("enName");
//            if (tmp != null)
//                countryDetails.enName = tmp.toString();
//            tmp = loc.get("zhName");
//            if (tmp != null)
//                countryDetails.zhName = tmp.toString();
//            tmp = loc.get("defaultCurrency");
//            if (tmp != null)
//                countryDetails.defCurrency = tmp.toString();
//
//            ds.save(countryDetails);
//        }
//
//        return Results.ok();
//    }
//
//    private static models.morphia.geo.Locality locImport(ObjectId id) throws TravelPiException, NoSuchFieldException, IllegalAccessException {
//        Morphia morphia = Utils.getMorphia();
//        Datastore ds = Utils.getDatastore();
//        morphia.map(models.morphia.geo.Country.class);
//        morphia.map(models.morphia.geo.Locality.class);
//
//        DBCollection col = Utils.getMongoClient().getDB("geo").getCollection("locality");
//        DBObject loc = col.findOne(BasicDBObjectBuilder.start("_id", id).get());
//        if (loc == null)
//            return null;
//
//        Object tmp;
//
//        models.morphia.geo.Locality locality = new models.morphia.geo.Locality();
//
//        locality.id = (ObjectId) loc.get("_id");
//        locality.zhName = loc.get("zhName").toString();
//        locality.baiduId = (int) loc.get("baiduId");
//        tmp = loc.get("level");
//        locality.level = (Integer) tmp;
//
//        tmp = loc.get("ratings");
//        if (tmp != null && tmp instanceof DBObject) {
//            Object tmp2 = ((DBObject) tmp).get("score");
//            if (tmp2 != null && tmp2 instanceof Number) {
//                Ratings r = new Ratings();
//                r.score = ((Number) tmp2).intValue();
//                locality.ratings = r;
//            }
//        }
//
//        tmp = loc.get("travelMonth");
//        if (tmp != null)
//            locality.travelMonth = Arrays.asList(((BasicDBList) tmp).toArray(new Integer[]{0}));
//        tmp = loc.get("alias");
//        if (tmp != null)
//            locality.alias = Arrays.asList(((BasicDBList) tmp).toArray(new String[]{""}));
//        tmp = loc.get("tags");
//        if (tmp != null)
//            locality.tags = Arrays.asList(((BasicDBList) tmp).toArray(new String[]{""}));
//        tmp = loc.get("imageList");
//        if (tmp != null)
//            locality.imageList = Arrays.asList(((BasicDBList) tmp).toArray(new String[]{""}));
//
//        for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
//            tmp = loc.get(k);
//            if (tmp != null && tmp instanceof Double) {
//                double val = (Double) tmp;
//                Field field = models.morphia.geo.Locality.class.getField(k);
//                field.set(locality, val);
//            }
//        }
//
//        tmp = loc.get("provCap");
//        locality.provCap = (!(tmp == null || !(tmp instanceof Boolean)));
//
//        tmp = loc.get("desc");
//        if (tmp != null)
//            locality.desc = tmp.toString();
//
//        ds.save(locality);
//
//        return locality;
//
//    }
//
//    public static Result localityImport() throws TravelPiException, IllegalAccessException, NoSuchFieldException {
//        Morphia morphia = Utils.getMorphia();
//        morphia.map(models.morphia.geo.Country.class);
//        morphia.map(models.morphia.geo.Locality.class);
//
//        DBCollection col = Utils.getMongoClient().getDB("geo").getCollection("locality");
//
//        DBCursor cursor = col.find(QueryBuilder.start().get(), BasicDBObjectBuilder.start("_id", 1).get());
//
//        while (cursor.hasNext()) {
//            DBObject loc = cursor.next();
//            locImport((ObjectId) loc.get("_id"));
//        }
//
//        return Results.ok();
//
//    }
}
