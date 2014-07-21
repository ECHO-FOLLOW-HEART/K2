package scripts;

import com.mongodb.*;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Address;
import models.morphia.geo.Coords;
import models.morphia.misc.CheckinRatings;
import models.morphia.misc.Contact;
import models.morphia.misc.Ratings;
import models.morphia.misc.SimpleRef;
import models.morphia.plan.Plan;
import models.morphia.plan.PlanDayEntry;
import models.morphia.plan.PlanItem;
import models.morphia.poi.Restaurant;
import models.morphia.poi.ViewSpot;
import models.morphia.traffic.*;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import utils.FPUtils;
import utils.FilterDelegate;
import utils.MapDelegate;
import utils.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class Main {

    public static void importLoc() throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        DBCollection col = Utils.getMongoClient().getDB("geo").getCollection("locality_old");
        DBCursor cursor = col.find(QueryBuilder.start().get());

        int i = -1;
        while (cursor.hasNext()) {
            i++;
            DBObject loc = cursor.next();

            Object tmp;

            models.morphia.geo.Locality locality = new models.morphia.geo.Locality();

            locality.id = (ObjectId) loc.get("_id");
            locality.zhName = loc.get("zhName").toString();
            locality.baiduId = (int) loc.get("baiduId");
            tmp = loc.get("level");
            locality.level = (Integer) tmp;

            tmp = loc.get("ratings");
            if (tmp != null && tmp instanceof DBObject) {
                Object tmp2 = ((DBObject) tmp).get("score");
                if (tmp2 != null && tmp2 instanceof Number) {
                    Ratings r = new Ratings();
                    r.score = ((Number) tmp2).intValue();
                    locality.ratings = r;
                }
            }

            tmp = loc.get("travelMonth");
            if (tmp != null)
                locality.travelMonth = Arrays.asList(((BasicDBList) tmp).toArray(new Integer[]{0}));
            tmp = loc.get("alias");
            if (tmp != null)
                locality.alias = Arrays.asList(((BasicDBList) tmp).toArray(new String[]{""}));
            tmp = loc.get("tags");
            if (tmp != null)
                locality.tags = Arrays.asList(((BasicDBList) tmp).toArray(new String[]{""}));
            tmp = loc.get("imageList");
            if (tmp != null)
                locality.imageList = Arrays.asList(((BasicDBList) tmp).toArray(new String[]{""}));

            Coords coords = new Coords();
            for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
                tmp = loc.get(k);
                if (tmp != null && tmp instanceof Number)
                    Coords.class.getField(k).set(coords, tmp);
            }
            locality.coords = coords;

            tmp = loc.get("provCap");
            locality.provCap = (!(tmp == null || !(tmp instanceof Boolean)));

            tmp = loc.get("desc");
            if (tmp != null)
                locality.desc = tmp.toString();

            tmp = loc.get("parent");
            if (tmp != null && tmp instanceof DBObject) {
                SimpleRef ref = new SimpleRef();
                ref.id = (ObjectId) ((DBObject) tmp).get("_id");
                ref.zhName = (String) ((DBObject) tmp).get("name");
                locality.superAdm = ref;
            }

            tmp = loc.get("siblings");
            if (tmp != null && tmp instanceof BasicDBList) {
                List<SimpleRef> siblings = new ArrayList<>();
                for (Object tmp2 : (BasicDBList) tmp) {
                    DBObject sib = (DBObject) tmp2;
                    SimpleRef ref = new SimpleRef();
                    ref.id = (ObjectId) sib.get("_id");
                    ref.zhName = (String) sib.get("name");
                    siblings.add(ref);
                }
                if (!siblings.isEmpty())
                    locality.sib = siblings;
            }

            locality.countryId = "CN";
            locality.countryZhName = "中国";

            ds.save(locality);
            System.out.println(String.format("%d: %s", i, locality.id.toString()));
        }
    }

    public static void importVs() throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection("view_spot");
//        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThanEquals(new ObjectId("53b0545e10114e051426e4fc")).get());
        DBCursor cursor = col.find(QueryBuilder.start().get());
        System.out.println(String.format("TOTAL RECORDS: %d", cursor.count()));

        int i = 0;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject poiDB = cursor.next();

//            Hotel poi = new Hotel();
            ViewSpot poi = new ViewSpot();
            poi.id = (ObjectId) poiDB.get("_id");
            poi.name = poiDB.get("name").toString();
            tmp = poiDB.get("url");
            if (tmp != null)
                poi.url = tmp.toString();
            tmp = poiDB.get("price");
            if (tmp != null && tmp instanceof Number)
                poi.price = ((Number) tmp).doubleValue();
            tmp = poiDB.get("priceDesc");
            if (tmp != null)
                poi.priceDesc = tmp.toString();
            tmp = poiDB.get("desc");
            if (tmp != null)
                poi.desc = tmp.toString();

            tmp = poiDB.get("imageList");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.imageList = imageList;
            }
            tmp = poiDB.get("alias");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.alias = imageList;
            }
            tmp = poiDB.get("tags");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.tags = imageList;
            }
            tmp = poiDB.get("ratings");
            if (tmp != null && tmp instanceof DBObject) {
                CheckinRatings r = new CheckinRatings();
                Object tmp2;

                tmp2 = ((DBObject) tmp).get("level");
                if (tmp2 != null && tmp2 instanceof Number)
                    poi.rankingA = ((Number) tmp2).intValue();

                tmp2 = ((DBObject) tmp).get("score");
                if (tmp2 != null && tmp2 instanceof Number)
                    r.score = ((Number) tmp2).intValue();

                tmp2 = ((DBObject) tmp).get("foodIndex");
                if (tmp2 != null && tmp2 instanceof Number)
                    r.dinningIdx = ((Number) tmp2).intValue();

                tmp2 = ((DBObject) tmp).get("shoppingIndex");
                if (tmp2 != null && tmp2 instanceof Number)
                    r.shoppingIdx = ((Number) tmp2).intValue();

                poi.ratings = r;
            }
            tmp = poiDB.get("tel");
            if (tmp != null) {
                Contact c = new Contact();
                c.phoneList = new ArrayList<>();
                c.phoneList.add(tmp.toString());
                poi.contact = c;
            }
            tmp = poiDB.get("geo");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject geo = (DBObject) tmp;
                Address addr = new Address();

                Coords coords = new Coords();
                for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
                    Object tmp2 = geo.get(k);
                    if (tmp2 == null || !(tmp2 instanceof Number))
                        continue;
                    double v = ((Number) tmp2).doubleValue();

                    Field field = coords.getClass().getField(k);
                    field.set(coords, v);
                }
                addr.coords = coords;

                SimpleRef loc = new SimpleRef();
                loc.id = (ObjectId) geo.get("locId");
                loc.zhName = geo.get("locName").toString();
                addr.loc = loc;

                Object tmp2 = geo.get("addr");
                if (tmp2 != null)
                    addr.address = tmp2.toString();
                poi.addr = addr;
            }
            tmp = poiDB.get("spotId");
            if (tmp != null && tmp instanceof Number)
                poi.spotId = ((Number) tmp).intValue();
            tmp = poiDB.get("isWorldHeritage");
            if (tmp != null && tmp instanceof Boolean)
                poi.worldHeritage = (Boolean) tmp;
            tmp = poiDB.get("contact");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject tmp2 = (DBObject) tmp;
                Object tmp3 = tmp2.get("tel");
                if (tmp3 != null) {
                    Contact c = new Contact();
                    c.phoneList = Arrays.asList(tmp3.toString());
                    poi.contact = c;
                }
            }
            tmp = poiDB.get("openTime");
            if (tmp != null)
                poi.openTime = tmp.toString();

            System.out.println(String.format("%d: %s", i, poi.id.toString()));
            ds.save(poi);
        }
    }

    public static void importPoi() throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        DBCollection col = Utils.getMongoClient().getDB("poi").getCollection("restaurant_old");
//        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThan(new ObjectId("53b0d3dd10114e05e449b006")).get());
        DBCursor cursor = col.find(QueryBuilder.start().get());
        System.out.println(String.format("TOTAL RECORDS: %d", cursor.count()));

        int i = -1;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject poiDB = cursor.next();

            Restaurant poi = new Restaurant();
            poi.id = (ObjectId) poiDB.get("_id");
            poi.name = poiDB.get("name").toString();
            tmp = poiDB.get("url");
            if (tmp != null)
                poi.url = tmp.toString();
            tmp = poiDB.get("price");
            if (tmp != null && tmp instanceof Number)
                poi.price = ((Number) tmp).doubleValue();
            tmp = poiDB.get("priceDesc");
            if (tmp != null)
                poi.priceDesc = tmp.toString();
            tmp = poiDB.get("desc");
            if (tmp != null)
                poi.desc = tmp.toString();

            tmp = poiDB.get("imageList");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.imageList = imageList;
            }
            tmp = poiDB.get("alias");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.alias = imageList;
            }
            tmp = poiDB.get("tags");
            if (tmp != null && tmp instanceof BasicDBList) {
                BasicDBList imageListDB = (BasicDBList) tmp;
                List<String> imageList = new ArrayList<>();
                for (Object obj : imageListDB)
                    imageList.add(obj.toString());
                poi.tags = imageList;
            }
            tmp = poiDB.get("ratings");
            if (tmp != null && tmp instanceof DBObject) {
                Object tmp2 = ((DBObject) tmp).get("score");
                if (tmp2 != null && tmp2 instanceof Number) {
                    CheckinRatings r = new CheckinRatings();
                    r.score = ((Number) tmp2).intValue();
                    poi.ratings = r;
                }
            }
            tmp = poiDB.get("tel");
            if (tmp != null) {
                Contact c = new Contact();
                c.phoneList = new ArrayList<>();
                c.phoneList.add(tmp.toString());
                poi.contact = c;
            }
            tmp = poiDB.get("geo");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject geo = (DBObject) tmp;
                Address addr = new Address();

                Coords coords = new Coords();
                for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
                    Object tmp2 = geo.get(k);
                    if (tmp2 == null || !(tmp2 instanceof Number))
                        continue;
                    double v = ((Number) tmp2).doubleValue();

                    Field field = coords.getClass().getField(k);
                    field.set(coords, v);
                }
                addr.coords = coords;

                SimpleRef loc = new SimpleRef();
                loc.id = (ObjectId) geo.get("locId");
                loc.zhName = geo.get("locName").toString();
                addr.loc = loc;

                Object tmp2 = geo.get("addr");
                if (tmp2 != null)
                    addr.address = tmp2.toString();
                poi.addr = addr;
            }

            System.out.println(String.format("%d: %s", i, poi.id.toString()));
            ds.save(poi);
        }
    }

    public static void importAirline() throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        DBCollection col = Utils.getMongoClient().getDB("traffic").getCollection("airline");
//        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThan(new ObjectId("53b05a7e10114e05e4483b47")).get());
        DBCursor cursor = col.find(QueryBuilder.start().get());
        System.out.println(String.format("TOTAL RECORDS: %d", cursor.count()));

        int i = 0;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject poiDB = cursor.next();

            Airline node = new Airline();
            node.id = (ObjectId) poiDB.get("_id");
            node.code = poiDB.get("code").toString();
            node.name = poiDB.get("name").toString();
            tmp = poiDB.get("fullName");
            if (tmp != null)
                node.fullName = tmp.toString();
            tmp = poiDB.get("shortName");
            if (tmp != null)
                node.shortName = tmp.toString();

            System.out.println(String.format("%d: %s", i, node.id.toString()));
            ds.save(node);
        }
    }


    public static void importAirport() throws TravelPiException {
        DB db = Utils.getMongoClient().getDB("traffic");
        DBCollection col = db.getCollection("airport");
        DBCursor cursor = col.find(QueryBuilder.start().get());

        MorphiaFactory factory = MorphiaFactory.getInstance();
        Datastore ds = factory.getDatastore(MorphiaFactory.DBType.TRAFFIC);

        while (cursor.hasNext()) {
            Object tmp;
            DBObject ap = cursor.next();
//            Airport airPort = airPortfromOldDb((ObjectId) loc.get("_id"));

            Airport airport = new models.morphia.traffic.Airport();
            airport.id = (ObjectId) ap.get("_id");
            airport.zhName = (String) ap.get("name");
            airport.enName = (String) ap.get("enName");
            airport.url = (String) ap.get("url");
            airport.desc = (String) ap.get("desc");

            tmp = ap.get("tel");
            if (tmp != null) {
                Contact c = new Contact();
                c.phoneList = Arrays.asList(tmp.toString());
                airport.contact = c;
            }

            Object alias = ap.get("alias");
            if (alias != null) {
                airport.alias = Arrays.asList(((BasicDBList) alias).toArray(new String[]{""}));
            }
            Object geo = ap.get("geo");
            if (geo != null) {
                Address address = new models.morphia.geo.Address();
                Double lat = (Double) ((BasicDBObject) geo).get("lat");
                Double lng = (Double) ((BasicDBObject) geo).get("lng");
                address.address = (String) ((BasicDBObject) geo).get("addr");
                Coords coords = new models.morphia.geo.Coords();
                coords.lat = lat;
                coords.lng = lng;
                address.coords = coords;
                Object loc = ((BasicDBObject) geo).get("locality");
                if (loc != null) {
                    ObjectId locId = (ObjectId) ((BasicDBObject) loc).get("id");
                    String locName = ((BasicDBObject) loc).get("localityName").toString();
                    SimpleRef ref = new SimpleRef();
                    ref.id = locId;
                    ref.zhName = locName;
                    address.loc = ref;
                }
                airport.addr = address;
            }

            ds.save(airport);
        }
    }


    public static void importAirRoute() throws TravelPiException, IllegalAccessException, NoSuchFieldException {
        DB db = Utils.getMongoClient().getDB("traffic");
        DBCollection col = db.getCollection("air_route");
        DBCursor cursor = col.find(QueryBuilder.start().get());

        MorphiaFactory factory = MorphiaFactory.getInstance();
        Datastore ds = factory.getDatastore(MorphiaFactory.DBType.TRAFFIC);

        int i = -1;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject ar = cursor.next();

            AirRoute airRoute = new AirRoute();
            airRoute.id = (ObjectId) ar.get("_id");
            tmp = ar.get("distance");
            if (tmp != null)
                airRoute.distance = ((Number) tmp).intValue();
            airRoute.code = (String) ar.get("code");
            tmp = ar.get("timeCost");
            if (tmp != null)
                airRoute.timeCost = ((Number) tmp).intValue();

            for (String k : new String[]{"depAirport", "arrAirport"}) {
                tmp = ar.get(k);
                if (tmp != null) {
                    SimpleRef ref = new SimpleRef();
                    BasicDBObject tmp2 = (BasicDBObject) tmp;
                    ref.id = (ObjectId) tmp2.get("_id");
                    ref.zhName = (String) tmp2.get("name");
                    Field field = AirRoute.class.getField(k);
                    field.set(airRoute, ref);
                }
            }
            for (Map.Entry<String, String> entry : (new HashMap<String, String>() {
                {
                    put("dep", "depLoc");
                    put("arr", "arrLoc");
                }
            }).entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();

                tmp = ar.get(k);
                if (tmp != null) {
                    SimpleRef ref = new SimpleRef();
                    BasicDBObject tmp2 = (BasicDBObject) tmp;
                    ref.id = (ObjectId) tmp2.get("_id");
                    ref.zhName = (String) tmp2.get("name");
                    Field field = AirRoute.class.getField(v);
                    field.set(airRoute, ref);
                }
            }
            for (String k : new String[]{"depTime", "arrTime"}) {
                tmp = ar.get(k);
                if (tmp == null || !(tmp instanceof Date))
                    continue;
                Field field = AirRoute.class.getField(k);
                field.set(airRoute, tmp);
            }

            tmp = ar.get("price");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject tmp2 = (DBObject) tmp;
                AirPrice price = new AirPrice();
                for (String k : new String[]{"price", "tax", "surcharge", "discount"}) {
                    Number val = (Number) tmp2.get(k);
                    if (val != null) {
                        Field field = AirPrice.class.getField(k);
                        field.set(price, val.doubleValue());
                    }
                }
                price.provider = (String) tmp2.get("provider");
                airRoute.price = price;
            }

            tmp = ar.get("carrier");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject tmp2 = (DBObject) tmp;
                SimpleRef carrier = new SimpleRef();
                carrier.id = (ObjectId) tmp2.get("_id");
                carrier.zhName = (String) tmp2.get("name");
                airRoute.carrier = carrier;
            }

            for (String k : new String[]{"selfChk", "meal", "nonStop", "arrTerm", "depTerm"}) {
                tmp = ar.get(k);
                if (tmp == null)
                    continue;
                Field field = AirRoute.class.getField(k);
                field.set(airRoute, (ar.get(k)));
            }

            tmp = ar.get("jetType");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject tmp2 = (DBObject) tmp;
                airRoute.jetName = (String) tmp2.get("short");
                airRoute.jetFullName = (String) tmp2.get("full");
            }

            ds.save(airRoute);
            System.out.println(String.format("%d: %s", i, airRoute.id.toString()));
        }
    }

    public static void main(String[] args) {
        System.out.println("HelloWorld");
        try {
//            importPoi();
//            importAirline();
//            importTrainStation();
//            importAirport();
//            importAirRoute();
//            importTrainRoute();
//            importPlan();
            importLoc();
        } catch (TravelPiException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static void calcDelta(final String dbName, final String colName, MorphiaFactory.DBType dbType) throws TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB(dbName).getCollection(colName);
        Datastore ds = MorphiaFactory.getInstance().getDatastore(dbType);

        // 查询差异
        DBCursor cursor = col.find(QueryBuilder.start().get(), BasicDBObjectBuilder.start("_id", 1).get());
        Set<ObjectId> oldStationList = new HashSet<>();
        for (Object tmp : cursor)
            oldStationList.add((ObjectId) tmp);
        Set<ObjectId> newStationList = new HashSet<>();
        Query<TrainStation> query = ds.createQuery(TrainStation.class).retrievedFields(true, "_id");
        for (TrainStation station : query)
            newStationList.add(station.id);
        // 旧的有，新的没有
        final Set<ObjectId> delta1 = new HashSet<>();
        delta1.addAll(oldStationList);
        delta1.removeAll(newStationList);
        // 新的有，旧的没有
        final Set<ObjectId> delta2 = new HashSet<>();
        delta2.addAll(newStationList);
        delta2.removeAll(oldStationList);

        HashMap<String, Set<ObjectId>> dtList = new HashMap<String, Set<ObjectId>>() {
            {
                put("delta1", delta1);
                put("delta2", delta2);
            }
        };
        for (String dt : dtList.keySet()) {
            Set<ObjectId> s = dtList.get(dt);
            String fileName = String.format("%s-%s-%s.txt", dbName, colName, dt);
            FileWriter writer = null;
            try {
                writer = new FileWriter(fileName);
                for (ObjectId id : s)
                    writer.write(id.toString() + '\n');
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null)
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    private static void importTrainRoute() throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        DBCollection col = Utils.getMongoClient().getDB("traffic").getCollection("train_route");
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        DBCursor cursor = col.find(QueryBuilder.start().get());
//        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThan(new ObjectId("53abe38410114e5847e7043d")).get());
        int i = -1;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject node = cursor.next();
            TrainRoute route = new TrainRoute();
            route.id = (ObjectId) node.get("_id");

            route.code = (String) node.get("code");
            route.distance = (int) node.get("distance");
            route.timeCost = (int) node.get("timeCost");
            route.type = (String) node.get("type");

            for (String k : new String[]{"arrStop", "depStop"}) {
                tmp = node.get(k);
                if (tmp == null || !(tmp instanceof DBObject))
                    continue;
                DBObject stopNode = (DBObject) tmp;
                SimpleRef stop = new SimpleRef();
                stop.id = (ObjectId) stopNode.get("_id");
                stop.zhName = (String) stopNode.get("name");
                Field field = TrainRoute.class.getField(k);
                field.set(route, stop);
            }

            for (Map.Entry<String, String> entry : new HashMap<String, String>() {
                {
                    put("arr", "arrLoc");
                    put("dep", "depLoc");
                }
            }.entrySet()) {
                String kNode = entry.getKey();
                String kRoute = entry.getValue();

                tmp = node.get(kNode);
                if (tmp == null || !(tmp instanceof DBObject))
                    continue;
                DBObject locNode = (DBObject) tmp;
                SimpleRef loc = new SimpleRef();
                loc.id = (ObjectId) locNode.get("_id");
                loc.zhName = (String) locNode.get("name");
                Field field = TrainRoute.class.getField(kRoute);
                field.set(route, loc);
            }

            tmp = node.get("price");
            if (tmp != null && tmp instanceof DBObject) {
                DBObject priceNode = (DBObject) tmp;
                Map<String, Double> price = new HashMap<>();
                for (String k : priceNode.keySet()) {
                    Object tmp2 = priceNode.get(k);
                    if (tmp2 != null && tmp2 instanceof Number)
                        price.put(k, ((Number) tmp2).doubleValue());
                }
                route.price = price;
            }

            for (String k : new String[]{"arrStop", "depStop"}) {
                tmp = node.get(k);
                if (tmp == null || !(tmp instanceof DBObject))
                    continue;
                DBObject stopNode = (DBObject) tmp;
                SimpleRef stop = new SimpleRef();
                stop.id = (ObjectId) stopNode.get("_id");
                stop.zhName = (String) stopNode.get("name");
                Field field = TrainRoute.class.getField(k);
                field.set(route, stop);
            }

            for (String k : new String[]{"arrTime", "depTime"}) {
                tmp = node.get(k);
                if (tmp == null || !(tmp instanceof Date))
                    continue;
                Field field = TrainRoute.class.getField(k);
                field.set(route, tmp);
            }

            tmp = node.get("details");
            if (tmp != null && tmp instanceof BasicDBList) {
                List<TrainEntry> details = new ArrayList<>();
                for (Object tmp2 : (BasicDBList) tmp) {
                    TrainEntry ret = procDetails((DBObject) tmp2, route);
                    if (ret != null)
                        details.add(ret);
                }
                if (!details.isEmpty())
                    route.details = details;
            }

            ds.save(route);
            System.out.println(String.format("%d: %s, %s", i, route.code, route.id.toString()));
        }
    }

    /**
     * 处理列车详情，得到一个TrainEntry
     *
     * @param detailsNodes
     * @param route
     */
    private static TrainEntry procDetails(DBObject detailsNodes, TrainRoute route) throws NoSuchFieldException, IllegalAccessException {
        TrainEntry entry = new TrainEntry();

        entry.idx = (int) detailsNodes.get("idx");
        entry.distance = (int) detailsNodes.get("distance");
        SimpleRef stop = new SimpleRef();
        stop.id = (ObjectId) detailsNodes.get("stopId");
        stop.zhName = (String) detailsNodes.get("stopName");
        entry.stop = stop;
        SimpleRef loc = new SimpleRef();
        loc.id = (ObjectId) detailsNodes.get("locId");
        loc.zhName = (String) detailsNodes.get("locName");
        entry.loc = loc;


        Object tmp = detailsNodes.get("price");
        if (tmp != null && tmp instanceof DBObject) {
            DBObject priceNode = (DBObject) tmp;
            Map<String, Double> price = new HashMap<>();
            for (String k : priceNode.keySet()) {
                Object tmp2 = priceNode.get(k);
                if (tmp2 != null && tmp2 instanceof Number)
                    price.put(k, ((Number) tmp2).doubleValue());
            }
            if (!price.isEmpty())
                entry.price = price;
        }

        for (String k : new String[]{"arrTime", "depTime"}) {
            tmp = detailsNodes.get(k);
            if (tmp != null && tmp instanceof Date) {
                Field field = TrainEntry.class.getField(k);
                field.set(entry, tmp);
            }
        }

        return entry;
    }

    private static void importPlan() throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        DBCollection col = Utils.getMongoClient().getDB("plan").getCollection("template");
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        DBCursor cursor = col.find(QueryBuilder.start().get());
//        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThan(new ObjectId("53abe38410114e5847e7043d")).get());
        int i = -1;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject planNode = cursor.next();

            Plan plan = new Plan();
            plan.id = (ObjectId) planNode.get("_id");
            tmp = planNode.get("loc");
            if (tmp != null && tmp instanceof DBObject) {
                SimpleRef ref = new SimpleRef();
                ref.id = (ObjectId) ((DBObject) tmp).get("_id");
                ref.zhName = (String) ((DBObject) tmp).get("name");
                plan.target = ref;
            }
            CheckinRatings r = new CheckinRatings();
            r.viewCnt = (Integer) planNode.get("viewCnt");
            plan.ratings = r;

            for (String k : new String[]{"tags", "imageList"}) {
                tmp = planNode.get(k);
                if (tmp != null && tmp instanceof BasicDBList) {
                    List<String> ret = FPUtils.map((BasicDBList) tmp, new MapDelegate<Object, String>() {
                        @Override
                        public String map(Object obj) {
                            return (obj != null ? obj.toString() : null);
                        }
                    });
                    ret = FPUtils.filter(ret, new FilterDelegate<String>() {
                        @Override
                        public boolean filter(String item) {
                            return (item != null);
                        }
                    });
                    if (!ret.isEmpty())
                        Plan.class.getField(k).set(plan, ret);
                }
            }

            for (String k : new String[]{"title", "desc"}) {
                tmp = planNode.get(k);
                Plan.class.getField(k).set(plan, (String) tmp);
            }

            for (String k : new String[]{"planId", "days"}) {
                tmp = planNode.get(k);
                if (tmp != null && tmp instanceof Number)
                    Plan.class.getField(k).set(plan, ((Number) tmp).intValue());
            }

            tmp = planNode.get("details");
            if (tmp != null && tmp instanceof BasicDBList) {
                List<PlanDayEntry> details = new ArrayList<>();
                for (Object tmp2 : ((BasicDBList) tmp)) {
                    if (tmp2 == null)
                        continue;

                    Object tmp3 = ((DBObject) tmp2).get("actv");
                    if (tmp3 == null)
                        continue;

                    List<PlanItem> activities = new ArrayList<>();
                    for (Object tmp4 : (BasicDBList) tmp3) {
                        DBObject itemNode = (DBObject) tmp4;
                        PlanItem planItem = new PlanItem();
                        SimpleRef ref = new SimpleRef();
                        ref.id = (ObjectId) itemNode.get("itemId");
                        ref.zhName = (String) itemNode.get("itemName");
                        planItem.item = ref;
                        ref = new SimpleRef();
                        ref.id = (ObjectId) itemNode.get("locId");
                        ref.zhName = (String) itemNode.get("locName");
                        planItem.loc = ref;
                        planItem.idx = (Integer) itemNode.get("idx");
                        activities.add(planItem);
                    }
                    if (!activities.isEmpty()) {
                        PlanDayEntry entry = new PlanDayEntry();
                        entry.actv = activities;
                        details.add(entry);
                    }
                }
                if (!details.isEmpty())
                    plan.details = details;
            }

            ds.save(plan);
            System.out.println(String.format("%d: %s", i, plan.id.toString()));
        }
    }

    private static void importTrainStation() throws TravelPiException {
        DBCollection col = Utils.getMongoClient().getDB("traffic").getCollection("train_station");
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        DBCursor cursor = col.find(QueryBuilder.start().get());
//        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThan(new ObjectId("53abe38410114e5847e7043d")).get());
        int i = -1;
        while (cursor.hasNext()) {
            i++;
            Object tmp;
            DBObject st = cursor.next();
            TrainStation station = new TrainStation();
            station.id = (ObjectId) st.get("_id");
            station.zhName = (String) st.get("name");
            station.enName = (String) st.get("enName");
            station.url = (String) st.get("url");
            station.desc = (String) st.get("desc");

            tmp = st.get("tel");
            if (tmp != null) {
                Contact c = new Contact();
                c.phoneList = Arrays.asList(tmp.toString());
                station.contact = c;
            }

            tmp = st.get("py");
            if (tmp != null && tmp instanceof BasicDBList && !((BasicDBList) tmp).isEmpty())
                station.py = Arrays.asList(((BasicDBList) tmp).toArray(new String[]{""}));

            Object alias = st.get("alias");
            if (alias != null) {
                station.alias = Arrays.asList(((BasicDBList) alias).toArray(new String[]{""}));
            }
            Object geo = st.get("geo");
            if (geo != null) {
                Address address = new models.morphia.geo.Address();
                Double lat = (Double) ((BasicDBObject) geo).get("lat");
                Double lng = (Double) ((BasicDBObject) geo).get("lng");
                address.address = (String) ((BasicDBObject) geo).get("addr");
                Coords coords = new models.morphia.geo.Coords();
                coords.lat = lat;
                coords.lng = lng;
                address.coords = coords;

                ObjectId locId = (ObjectId) ((DBObject) geo).get("locId");
                String locName = (String) ((DBObject) geo).get("locName");
                if (locId != null && locName != null) {
                    SimpleRef ref = new SimpleRef();
                    ref.id = locId;
                    ref.zhName = locName;
                    address.loc = ref;
                } else
                    System.out.println(String.format("Invalid station: %s", station.id.toString()));
                station.addr = address;
            }

            ds.save(station);
            System.out.println(String.format("%d: %s", i, station.id.toString()));
        }
    }
}
