package models;

import com.mongodb.MongoClient;
import exception.ErrorCode;
import exception.TravelPiException;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
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

    private final Morphia morphia;
    private final MongoClient client;

    public enum DBType {
        GEO,
        POI,
        PLAN,
        USER,
        MISC
    }

    private static Hashtable<DBType, Datastore> dsMap = new Hashtable<>();

    private static MorphiaFactory ourInstance;

    public synchronized static MorphiaFactory getInstance() throws TravelPiException {
        if (ourInstance == null)
            ourInstance = new MorphiaFactory();

        return ourInstance;
    }

    public Morphia getMorphia() {
        return morphia;
    }

    private MorphiaFactory() throws TravelPiException {
        Configuration config = Configuration.root();
        String host = "localhost";
        int port = 27017;
        Map mongo = (Map) config.getObject("mongodb");
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
        morphia.map(models.morphia.misc.MiscInfo.class);
//        morphia.mapPackage("models.morphia", true);
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
            case USER:
                ds = morphia.createDatastore(client, "user");
                break;
            case MISC:
                ds = morphia.createDatastore(client, "misc");
                break;
        }
        if (ds != null)
            dsMap.put(type, ds);
        return ds;
    }
}
