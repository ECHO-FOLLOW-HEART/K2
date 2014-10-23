package models;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.misc.ValidationCode;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.ValidationExtension;
import play.Configuration;

import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Morphia生成工厂。
 *
 * @author Zephyre
 */
public class MorphiaFactory {

    private static Hashtable<DBType, Datastore> dsMap = new Hashtable<>();
    private static MorphiaFactory ourInstance;
    private final Morphia morphia;
    private final MongoClient client;

//    private MorphiaFactory(String host, int port) throws TravelPiException {
//        try {
//            client = new MongoClient(host, port);
//        } catch (UnknownHostException e) {
//            throw new TravelPiException(ErrorCode.DATABASE_ERROR, "Invalid database connection.");
//        }
//
//        morphia = new Morphia();
//        new ValidationExtension(morphia);
//    }

    private MorphiaFactory() throws TravelPiException {
        Configuration config = Configuration.root();

        Map mongo = (Map) config.getObject("mongodb");
        String host = null;
        int port = 0;
        if (mongo != null) {
            host = mongo.get("host").toString();
            port = Integer.parseInt(mongo.get("port").toString());
        }
        try {
            client = new MongoClient(host, port);
        } catch (UnknownHostException e) {
            throw new TravelPiException(ErrorCode.DATABASE_ERROR, "Invalid database connection.");
        }

        morphia = new Morphia();
        new ValidationExtension(morphia);

//        morphia.map(MiscInfo.class);
//        morphia.map(Locality.class);
//        morphia.map(Country.class);
//        morphia.map(Restaurant.class);
//        morphia.map(Hotel.class);
//        morphia.map(ViewSpot.class);


//        morphia.mapPackage("models.morphia", true);
    }

    public synchronized static MorphiaFactory getInstance() throws TravelPiException {
        if (ourInstance == null)
            ourInstance = new MorphiaFactory();

        return ourInstance;
    }

//    public synchronized static MorphiaFactory getInstance(String host, int port) throws TravelPiException {
//        if (ourInstance == null)
//            ourInstance = new MorphiaFactory(host, port);
//
//        return ourInstance;
//    }

    public Morphia getMorphia() {
        return morphia;
    }

    public synchronized Datastore getDatastore(DBType type) throws TravelPiException {
        // 初始化
        getInstance();

        if (dsMap.contains(type))
            return dsMap.get(type);

        Datastore ds = null;
        switch (type) {
            case GEO:
                ds = morphia.createDatastore(client, "geo");
                break;
            case POI:
                ds = morphia.createDatastore(client, "poi");
                break;
            case PLAN:
                ds = morphia.createDatastore(client, "plan");
                break;
            case PLAN_UGC:
                ds = morphia.createDatastore(client, "plan_ugc");
                break;
            case USER:
                ds = morphia.createDatastore(client, "user");
                break;
            case MISC:
                ds = morphia.createDatastore(client, "misc");
                break;
            case TRAFFIC:
                ds = morphia.createDatastore(client, "traffic");
                break;
        }
        try {
            if (ds != null) {
                ds.ensureIndexes();
                ds.ensureCaps();
                dsMap.put(type, ds);
            }
        } catch (MongoClientException e) {
            throw new TravelPiException(ErrorCode.DATABASE_ERROR, "Database error.", e);
        }

        return ds;
    }

    public enum DBType {
        GEO,
        POI,
        PLAN,
        PLAN_UGC,
        USER,
        MISC,
        TRAFFIC
    }
}
