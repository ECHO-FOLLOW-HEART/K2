package aizou.core;

import exception.AizouException;
import exception.ErrorCode;
import models.AizouBaseEntity;
import models.MorphiaFactory;
import models.geo.Country;
import models.geo.Locality;
import models.poi.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import play.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * POI相关核心接口。
 *
 * @author Zephyre
 */
public class WebPoiAPI {

    /**
     * 获得POI信息。
     *
     * @see PoiAPI#getPOIInfo(org.bson.types.ObjectId, PoiAPI.POIType, boolean)
     */
    public static AbstractPOI getPOIInfo(String poiId, PoiAPI.POIType poiType, boolean showDetails) throws AizouException {

        ObjectId objPoiId = new ObjectId(poiId);
        Class<? extends AbstractPOI> poiClass;
        switch (poiType) {
            case VIEW_SPOT:
                poiClass = ViewSpot.class;
                break;
            case HOTEL:
                poiClass = Hotel.class;
                break;
            case RESTAURANT:
                poiClass = Restaurant.class;
                break;
            case SHOPPING:
                poiClass = Shopping.class;
                break;
            default:
                throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid POI type.");
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        return ds.createQuery(poiClass).field("_id").equal(objPoiId).get();
    }

}

