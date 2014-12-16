package aizou.core;


import exception.AizouException;
import models.MorphiaFactory;
import models.geo.Country;
import models.geo.Locality;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by zephyre on 7/10/14.
 */
public class GeoAPI {

    /**
     * 获得国家详情
     *
     * @param countryId
     * @param field
     * @return
     */
    public static Country countryDetails(ObjectId countryId, List<String> field) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Country> query = ds.createQuery(Country.class).field("_id").equal(countryId);
        if (field != null && !field.isEmpty())
            query.retrievedFields(true, field.toArray(new String[field.size()]));

        return query.get();
    }

    /**
     * 获得城市详情。
     *
     * @param locId 城市ID。
     * @return 如果没有找到，返回null。
     * @throws exception.AizouException
     */
    public static Locality locDetails(ObjectId locId, List<String> fields) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class).field("_id").equal(locId);
        if (fields != null && !fields.isEmpty())
            query.retrievedFields(true, fields.toArray(new String[fields.size()]));
        return query.get();
    }

    /**
     * 获得城市详情。
     *
     * @param locId 城市ID。
     * @return 如果没有找到，返回null。
     * @throws exception.AizouException
     */
    public static Locality locDetails(String locId) throws AizouException {
        return locDetails(new ObjectId(locId), null);
    }

    /**
     * 通过关键词对目的地进行搜索。
     *
     * @param keyword  搜索关键词。
     * @param prefix   是否为前缀搜索？
     * @param page     分页偏移量。
     * @param pageSize 页面大小。
     */
    public static java.util.Iterator<Locality> searchLocalities(String keyword, boolean prefix, ObjectId countryId, int page, int pageSize)
            throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class);
        if (keyword != null && !keyword.isEmpty())
            query.or(
                    query.criteria("zhName").equal(Pattern.compile(prefix ? "^" + keyword : keyword)),
                    query.criteria("enName").equal(Pattern.compile("^" + keyword, Pattern.CASE_INSENSITIVE)),
                    query.criteria("alias").equal(Pattern.compile("^" + keyword, Pattern.CASE_INSENSITIVE))
            );
        if (countryId != null)
            query.field(String.format("%s.id", Locality.fnCountry)).equal(countryId);
        return query.order(String.format("-%s", Locality.fnHotness))
                .offset(page * pageSize).limit(pageSize).iterator();
    }

    /**
     * 根据名称搜索国家。
     *
     * @param keyword
     * @return
     * @throws exception.AizouException
     */
    public static List<Country> searchCountryByName(String keyword, int page, int pageSize) throws AizouException {
        Query<Country> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO).createQuery(Country.class);
        if (!keyword.equals("")) {
            query.or(
                    query.criteria("zhName").equal(keyword),
                    query.criteria("enName").equal(Pattern.compile("^" + keyword, Pattern.CASE_INSENSITIVE)),
                    query.criteria("alias").equal(Pattern.compile("^" + keyword, Pattern.CASE_INSENSITIVE))
            );
        }
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    /**
     * 根据名称搜索国家。
     *
     * @return
     * @throws exception.AizouException
     */
    public static List<Country> searchCountryByName(List<String> keywords, int page, int pageSize) throws AizouException {
        Query<Country> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO).createQuery(Country.class);
        if (keywords != null) {
            List<CriteriaContainerImpl> criList = new ArrayList<>();
            for (String word : keywords)
                criList.add(query.criteria("zhName").equal(Pattern.compile("^" + word)));
            query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        }
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    /**
     * 搜索目的地
     *
     * @param
     * @param page
     * @param pageSize
     * @return
     */
    public static List<Locality> getDestinations(boolean abroad, int page, int pageSize) throws AizouException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class);
        query.field("abroad").equal(abroad).field("enabled").equal(true);
        query.offset(page * pageSize).limit(pageSize);

        return query.asList();
    }

    public static List<Locality> getDestinationsByCountry(ObjectId countryID, int page, int pageSize) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class);
        query.field("country.id").equal(countryID);
        query.order("-hotness");
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }
}
