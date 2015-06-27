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
            servers.add(new ServerAddress(s.get("host").toString(), Integer.parseInt(s.get("port").toString())));
        }

        if (servers.isEmpty())
            throw new AizouException(ErrorCode.DATABASE_ERROR, "Invalid database connection settings.");

//        val user = conf.getString("yunkai.mongo.user")
//        val password = conf.getString("yunkai.mongo.password")
//        val dbName = conf.getString("yunkai.mongo.db")
//        val credential = MongoCredential.createScramSha1Credential(user, dbName, password.toCharArray)

//        credential = MongoCredential.create
        client = new MongoClient(servers);

        morphia = new Morphia();
        new ValidationExtension(morphia);

        morphia.mapPackage("models.geo", true);
        morphia.mapPackage("models.guide", true);
        morphia.mapPackage("models.misc", true);
        morphia.mapPackage("models.plan", true);
        morphia.mapPackage("models.poi", true);
        morphia.mapPackage("models.traffic", true);
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

        Datastore ds = morphia.createDatastore(client, "k2-dev");
        try {
            ds.ensureIndexes();
            ds.ensureCaps();
            dsMap.put(type, ds);
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
        IMAGESTORE,
        TRAVELNOTE
    }
}
