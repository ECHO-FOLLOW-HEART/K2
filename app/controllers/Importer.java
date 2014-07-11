package controllers;

import com.mongodb.*;
import exception.TravelPiException;
import models.Coords;
import models.MorphiaFactory;
import models.morphia.geo.Country;
import models.morphia.geo.Locality;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import utils.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 导入
 * <p/>
 * Created by zephyre on 7/11/14.
 */
public class Importer extends Controller {
    public static Result countryImport() throws TravelPiException {
        Morphia morphia = Utils.getMorphia();
        Datastore ds = Utils.getDatastore();
        morphia.map(Country.class);

        DBCollection col = Utils.getMongoClient().getDB("geo").getCollection("country");

        DBCursor cursor = col.find(QueryBuilder.start().get());
        while (cursor.hasNext()) {
            DBObject loc = cursor.next();

            Country country = new Country();
            country.code = loc.get("_id").toString();
            Object tmp = loc.get("code3");
            if (tmp != null)
                country.code3 = tmp.toString();
            tmp = loc.get("enName");
            if (tmp != null)
                country.enName = tmp.toString();
            tmp = loc.get("zhName");
            if (tmp != null)
                country.zhName = tmp.toString();
            tmp = loc.get("defaultCurrency");
            if (tmp != null)
                country.defCurrency = tmp.toString();

            ds.save(country);
        }

        return Results.ok();
    }


    public static Result localityImport() throws TravelPiException, IllegalAccessException, NoSuchFieldException {
        DB db = Utils.getMongoClient().getDB("geo");
        Set<String> colLIst = db.getCollectionNames();
        DBCollection col = db.getCollection("locality_old");
        DBCursor cursor = col.find(QueryBuilder.start().get(), BasicDBObjectBuilder.start("_id", 1).get());

        MorphiaFactory factory = MorphiaFactory.getInstance();
        Datastore ds = factory.getDatastore(MorphiaFactory.DBType.GEO);

        while (cursor.hasNext()) {
            DBObject loc = cursor.next();
            locImport((ObjectId) loc.get("_id"), ds);
        }

        return Results.ok();

    }


    private static models.morphia.geo.Locality locImport(ObjectId id, Datastore ds) throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        DBCollection col = Utils.getMongoClient().getDB("geo").getCollection("locality_old");
        DBObject loc = col.findOne(BasicDBObjectBuilder.start("_id", id).get());
        if (loc == null)
            return null;

        Object tmp;

        models.morphia.geo.Locality locality = new models.morphia.geo.Locality();

        locality.id = (ObjectId) loc.get("_id");
        locality.zhName = loc.get("zhName").toString();
        locality.baiduId = (int) loc.get("baiduId");
        tmp = loc.get("level");
        locality.level = (Integer) tmp;

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

        Coords coords = new Coords();// locality.coords;
        for (String k : new String[]{"lat", "lng", "blat", "blng"}) {
            tmp = loc.get(k);
            if (tmp != null && tmp instanceof Double) {
                double val = (Double) tmp;
                Field field = Coords.class.getField(k);
                field.set(coords, val);
            }
        }
        locality.coords = coords;

        locality.country = ds.createQuery(Country.class).field("_id").equal("CN").get();

        tmp = loc.get("parent");
        if (tmp != null) {
            ObjectId pid = (ObjectId) ((DBObject) tmp).get("_id");
            models.morphia.geo.Locality parent = ds.createQuery(models.morphia.geo.Locality.class).field("_id").equal(pid).get();
            if (parent == null)
                parent = locImport(pid, ds);

            locality.parent = parent;
        }

//        tmp = loc.get("siblings");
//        if (tmp != null && tmp instanceof BasicDBList) {
//            List<Locality> siblings = new ArrayList<>();
//            for (Object tmp1 : (BasicDBList) tmp) {
//                DBObject sibNode = (DBObject) tmp1;
//                ObjectId sid = (ObjectId) (sibNode.get("_id"));
//                models.morphia.geo.Locality sib = ds.createQuery(models.morphia.geo.Locality.class).field("_id").equal(sid).get();
//                if (sib == null)
//                    sib = locImport(sid, ds);
//
//                if (sib != null)
//                    siblings.add(sib);
//            }
//            locality.siblings = siblings;
//        }

        tmp = loc.get("provCap");
        locality.provCap = (!(tmp == null || !(tmp instanceof Boolean)));

        tmp = loc.get("desc");
        if (tmp != null)
            locality.desc = tmp.toString();

        ds.save(locality);

        return locality;

    }
}
