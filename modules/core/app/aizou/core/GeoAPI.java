package aizou.core;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.geo.Country;
import models.geo.Locality;
import models.misc.SimpleRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import utils.formatter.taozi.geo.SimpleCountryFormatter;

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
    public static Country countryDetails(String countryId, List<String> field) throws TravelPiException {
        return countryDetails(new ObjectId(countryId), field);
    }

    /**
     * 获得国家详情
     *
     * @param countryId
     * @param field
     * @return
     */
    public static Country countryDetails(ObjectId countryId, List<String> field) throws TravelPiException {
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
     * @throws exception.TravelPiException
     */
    public static Locality locDetails(ObjectId locId) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class).field("_id").equal(locId);
        return query.get();
    }

    /**
     * 获得城市详情。
     *
     * @param locId 城市ID。
     * @return 如果没有找到，返回null。
     * @throws exception.TravelPiException
     */
    public static Locality locDetails(String locId) throws TravelPiException {
        try {
            return locDetails(new ObjectId(locId));
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.", locId));
        }
    }

    /**
     * 通过关键词对城市进行搜索。
     *
     * @param keyword  搜索关键词。
     * @param prefix   是否为前缀搜索？
     * @param page     分页偏移量。
     * @param pageSize 页面大小。
     */
    public static java.util.Iterator<Locality> searchLocalities(String keyword, boolean prefix, ObjectId countryId, int page, int pageSize)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class);
        if (keyword != null && !keyword.isEmpty())
            query.filter("zhName", Pattern.compile(prefix ? "^" + keyword : keyword));
        if (countryId != null)
            query.field(String.format("%s.%s", Locality.fnCountry, SimpleRef.simpID)).equal(countryId);
        return query.offset(page * pageSize).limit(pageSize).iterator();
    }

    /**
     * 根据名称搜索国家。
     *
     * @param keyword
     * @return
     *
     * @throws TravelPiException
     */
    public static List<Country> searchCountryByName(String keyword, int page, int pageSize) throws TravelPiException {
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
     *
     * @throws TravelPiException
     */
    public static List<Country> searchCountryByName(List<String> keywords, int page, int pageSize) throws TravelPiException {
        Query<Country> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO).createQuery(Country.class);
        if (keywords!= null) {
            List<CriteriaContainerImpl> criList = new ArrayList<>();
            for (String word : keywords)
                criList.add(query.criteria("zhName").equal(Pattern.compile("^" + word)));
            query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        }
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    /**
     * 搜索国家信息
     *
     * @param keyword
     * @param page
     * @param pageSize
     * @return
     */
    public static List<JsonNode> searchCountry(String keyword, int page, int pageSize) throws TravelPiException {

        List<Country> countryList = searchCountryByName(keyword, page, pageSize);
        List<JsonNode> result = new ArrayList<>();
        for (Country c : countryList)
            result.add(new SimpleCountryFormatter().format(c));
        return result;
    }

    /**
     * 搜索目的地
     *
     * @param
     * @param page
     * @param pageSize
     * @return
     */
    public static List<Locality> getDestinations(boolean abroad, int page, int pageSize) throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class);
        query.field("abroad").equal(abroad).field("enabled").equal(true);
        query.offset(page * pageSize).limit(pageSize);

        return query.asList();
    }

    public static List<Locality> getDestinationsByCountry(ObjectId countryID, int page, int pageSize) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class);
        query.field("country.id").equal(countryID);
        query.order("-hotness");
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }
}
