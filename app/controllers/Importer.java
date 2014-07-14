package controllers;

import com.mongodb.*;
import com.mysql.fabric.xmlrpc.base.Array;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.MorphiaFactory.DBType;
import models.morphia.geo.Address;
import models.morphia.geo.Coords;
import models.morphia.geo.Country;
import models.morphia.geo.Locality;
import models.morphia.traffic.AirRoute;
import models.morphia.traffic.Airport;
import models.morphia.geo.Country;
import models.morphia.traffic.Airline;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import utils.Utils;

import java.lang.reflect.Field;
import java.util.Arrays;
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

//        locality.country = ds.createQuery(Country.class).field("_id").equal("CN").get();
//
//        tmp = loc.get("parent");
//        if (tmp != null) {
//            ObjectId pid = (ObjectId) ((DBObject) tmp).get("_id");
//            models.morphia.geo.Locality parent = ds.createQuery(models.morphia.geo.Locality.class).field("_id").equal(pid).get();
//            if (parent == null)
//                parent = locImport(pid, ds);
//
//            locality.parent = parent;
//        }

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

    public static Result airRouteImport(int start  , int count ) throws TravelPiException, IllegalAccessException, NoSuchFieldException {
        DB db = Utils.getMongoClient().getDB("traffic");
        DBCollection col = db.getCollection("air_route_old");
        DBCursor cursor = col.find(QueryBuilder.start().get(), BasicDBObjectBuilder.start("_id", 1).get());

        MorphiaFactory factory = MorphiaFactory.getInstance();
        Datastore ds = factory.getDatastore(DBType.TRAFFIC);

        while (cursor.hasNext()) {
            DBObject ar = cursor.next();
            AirRoute airRoute = airRouteFromOldDb((ObjectId) ar.get("_id"));
            if(airRoute!=null){
                ds.save(airRoute);
            }
        }
        return Results.ok();
    }
    
    private static models.morphia.traffic.AirRoute airRouteFromOldDb(ObjectId id) throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        DBCollection col = Utils.getMongoClient().getDB("traffic").getCollection("air_route_old");
        DBObject ar = col.findOne(BasicDBObjectBuilder.start("_id", id).get());
        if (ar == null)
            return null;

        models.morphia.traffic.AirRoute airRoute = new models.morphia.traffic.AirRoute();
        airRoute.id = (ObjectId) ar.get("_id");
        airRoute.distance = (Integer) ar.get("distance");
        airRoute.flightCode = (String) ar.get("code");
        
        Object tmp;
        tmp = ar.get("depAirport");
        if(tmp != null){
        	Airport depAirport = airPortfromOldDb((ObjectId) ((BasicDBObject)tmp).get("_id"));
        	airRoute.depAirport = depAirport;
        }
        
        tmp = ar.get("arrAirport");
        if(tmp != null){
        	Airport arrAirport = airPortfromOldDb((ObjectId) ((BasicDBObject)tmp).get("_id"));
        	airRoute.arrAirport = arrAirport;
        }
        tmp = ar.get("dep");
        if(tmp != null) {
            ObjectId depLocId = (ObjectId) ((BasicDBObject) tmp).get("_id");
            Locality depLoc = new Locality();
            depLoc.id = depLocId;
            airRoute.depLoc = depLoc;
        }
        tmp = ar.get("arr");
        if(tmp != null) {
            ObjectId arrLocId = (ObjectId) ((BasicDBObject) tmp).get("_id");
            Locality arrLoc = new Locality();
            arrLoc.id = arrLocId;
            airRoute.arrLoc = arrLoc;
        }
		return airRoute;
    }
    
    public static Result airportImport(int start  , int count ) throws TravelPiException, IllegalAccessException, NoSuchFieldException {
        DB db = Utils.getMongoClient().getDB("traffic");
        DBCollection col = db.getCollection("airport_old");
        DBCursor cursor = col.find(QueryBuilder.start().get(), BasicDBObjectBuilder.start("_id", 1).get());

        MorphiaFactory factory = MorphiaFactory.getInstance();
        Datastore ds = factory.getDatastore(DBType.TRAFFIC);
        
        while (cursor.hasNext()) {
            DBObject loc = cursor.next();
            Airport airPort = airPortfromOldDb((ObjectId) loc.get("_id"));
            if(airPort!=null){
            	ds.save(airPort);
            }
        }
        return Results.ok();
    }
    
    private static models.morphia.traffic.Airport airPortfromOldDb(ObjectId id) throws TravelPiException{
    	DBCollection col = Utils.getMongoClient().getDB("traffic").getCollection("airport_old");
        DBObject ap = col.findOne(BasicDBObjectBuilder.start("_id", id).get());
        if (ap == null)
            return null;
        Airport airport = new models.morphia.traffic.Airport();
        airport.id = (ObjectId) ap.get("_id");
        airport.zhName = (String) ap.get("name");
        airport.enName = (String) ap.get("enName");
        airport.url = (String) ap.get("url");
        airport.desc = (String) ap.get("desc");
        airport.tel = (String) ap.get("tel");
        Object alias = ap.get("alias");
        if(alias != null){
            airport.alias = Arrays.asList(((BasicDBList) alias).toArray(new String[]{""}));
        }
        Object geo = ap.get("geo");
        if(geo != null){
        	Address address = new models.morphia.geo.Address();
        	Double lat = (Double) ((BasicDBObject)geo).get("lat");
        	Double lng = (Double) ((BasicDBObject)geo).get("lng");
        	String addr = (String) ((BasicDBObject)geo).get("address");
        	address.address = addr;
        	Coords coords = new models.morphia.geo.Coords();
        	coords.lat = lat;
        	coords.lng = lng;
        	address.coords = coords;
            Object loc = ((BasicDBObject)geo).get("locality");
            if(loc != null){
            	ObjectId locId = (ObjectId) ((BasicDBObject)loc).get("id");
            	Locality locality = new Locality();
            	locality.id = locId;
            	address.loc = locality;
            }
            airport.address = address;
        }
		return airport;
    }
    

    public static Result test() {
        try {
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);

            Airline al = new Airline();

            al.code = "MU2843";
            al.name = "name";
            al.fullName = "fullName";

            ds.save(al);

            return Utils.createResponse(ErrorCode.NORMAL, "OK");

        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }
}
