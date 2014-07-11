package models;

import com.mongodb.MongoClient;
import exception.ErrorCode;
import exception.TravelPiException;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.net.UnknownHostException;
import java.util.Hashtable;

/**
 * Morphia生成工厂。
 *
 * @author Zephyre
 */
public class MorphiaFactory {

    private final Morphia morphia;

    public enum DBType {
        GEO,
        POI,
        PLAN,
        USER,
        MISC
    }

    private static Hashtable<DBType, Datastore> dsMap = new Hashtable<>();

    private static MorphiaFactory ourInstance = new MorphiaFactory();

    public static MorphiaFactory getInstance() {
        return ourInstance;
    }

    public Morphia getMorphia() {
        return morphia;
    }

    private MorphiaFactory() {
        morphia = new Morphia();
        morphia.map(models.morphia.misc.MiscInfo.class);
//        morphia.mapPackage("models.morphia", true);
    }

    public synchronized Datastore getDatastore(DBType type) throws TravelPiException {
        if (dsMap.contains(type))
            return dsMap.get(type);

        Datastore ds = null;
        try {
            switch (type) {
                case GEO:
                    ds = morphia.createDatastore(new MongoClient(), "geo");
                    break;
                case POI:
                    ds = morphia.createDatastore(new MongoClient(), "poi");
                    break;
                case PLAN:
                    ds = morphia.createDatastore(new MongoClient(), "plan");
                    break;
                case USER:
                    ds = morphia.createDatastore(new MongoClient(), "user");
                    break;
                case MISC:
                    ds = morphia.createDatastore(new MongoClient(), "misc");
                    break;
            }
            if (ds != null)
                dsMap.put(type, ds);
            return ds;
        } catch (UnknownHostException e) {
            throw new TravelPiException(ErrorCode.DATABASE_ERROR, "Invalid database connection.");
        }
    }
}
