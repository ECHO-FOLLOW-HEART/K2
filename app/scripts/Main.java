package scripts;

import com.mongodb.*;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Address;
import models.morphia.geo.Coords;
import models.morphia.misc.Contact;
import models.morphia.misc.SimpleRef;
import models.morphia.poi.Hotel;
import models.morphia.poi.Ratings;
import models.morphia.poi.Restaurant;
import models.morphia.poi.ViewSpot;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import utils.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void importVs() throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        DBCollection col = Utils.getMongoClient("localhost", 28017).getDB("poi").getCollection("view_spot");
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
                Ratings r = new Ratings();
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
        DBCollection col = Utils.getMongoClient("localhost", 28017).getDB("poi").getCollection("restaurant");
//        DBCursor cursor = col.find(QueryBuilder.start("_id").greaterThan(new ObjectId("53b0583210114e059545dc12")).get());
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
                    Ratings r = new Ratings();
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

    public static void main(String[] args) {
        System.out.println("HelloWorld");
        try {
            importPoi();
        } catch (TravelPiException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
