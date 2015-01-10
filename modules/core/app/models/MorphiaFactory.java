package models;

import com.mongodb.*;
import exception.AizouException;
import exception.ErrorCode;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.ValidationExtension;
import play.Configuration;
import play.Play;

import java.net.UnknownHostException;
import java.util.*;

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

    private MorphiaFactory() throws AizouException {
        Configuration config = Play.application().configuration();

        List<ServerAddress> servers = new ArrayList<>();
        Object confEntry = ((Map) config.asMap().get("mongodb")).get("serverList");
        if (confEntry == null) {
            Map<String, Object> tmp = new HashMap<>();
            tmp.put("host", "localhost");
            tmp.put("port", 27017);
            confEntry = Arrays.asList(tmp);
        }
        for (Object entry : (List) confEntry) {
            Map s = (Map) entry;
            try {
                servers.add(new ServerAddress(s.get("host").toString(), Integer.parseInt(s.get("port").toString())));
            } catch (UnknownHostException ignored) {
            }
        }

        if (servers.isEmpty())
            throw new AizouException(ErrorCode.DATABASE_ERROR, "Invalid database connection settings.");

        client = new MongoClient(servers);

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

    public synchronized static MorphiaFactory getInstance() throws AizouException {
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

    public synchronized Datastore getDatastore(DBType type) throws AizouException {
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
            case GUIDE:
                ds = morphia.createDatastore(client, "guide");
                break;
            case IMAGESTORE:
                ds = morphia.createDatastore(client, "imagestore");
                break;
        }
        try {
            if (ds != null) {
                ds.ensureIndexes();
                ds.ensureCaps();
                dsMap.put(type, ds);
            }
        } catch (MongoClientException e) {
            throw new AizouException(ErrorCode.DATABASE_ERROR, "Database error.", e);
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
        TRAFFIC,
        GUIDE,
        IMAGESTORE
    }
}
