package core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Country;
import models.morphia.geo.Locality;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Locality相关的核心接口。
 *
 * @author Zephyre
 */
public class LocalityAPI {

    /**
     * 获得国家详情
     *
     * @param countryId
     * @return
     * @throws TravelPiException
     */
    public static Country countryDetails(String countryId) throws TravelPiException {
        try {
            return countryDetails(new ObjectId(countryId));
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid country ID: %s.", countryId));
        }
    }

    /**
     * 获得国家详情
     *
     * @param countryId
     * @return
     * @throws TravelPiException
     */
    public static Country countryDetails(ObjectId countryId) throws TravelPiException {
        return MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO)
                .createQuery(Country.class).field("_id").equal(countryId).get();
    }

    /**
     * 获得城市详情。
     *
     * @param locId 城市ID。
     * @param level 查询级别。
     * @return 如果没有找到，返回null。
     * @throws TravelPiException
     */
    public static Locality locDetails(String locId, int level) throws TravelPiException {
        try {
            return locDetails(new ObjectId(locId), level);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality ID: %s.", locId));
        }
    }

    /**
     * 通过百度ID获得城市详情。
     *
     * @param baiduId
     * @return
     * @throws TravelPiException
     */
    public static Locality locDetailsBaiduId(int baiduId) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        return ds.createQuery(Locality.class).field("baiduId").equal(baiduId).get();
    }

    /**
     * 获得城市详情。
     *
     * @param locId 城市ID。
     * @return 如果没有找到，返回null。
     * @throws TravelPiException
     */
    public static Locality locDetails(ObjectId locId, int level) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class).field("_id").equal(locId);

        List<String> fields = new ArrayList<>();
        fields.addAll(Arrays.asList("zhName", "superAdm", "level"));
        if (level > 1)
            fields.addAll(Arrays.asList("desc", "imageList", "tags", "coords"));

        query.retrievedFields(true, fields.toArray(new String[]{""}));
        return query.get();
    }


    /**
     * 取得城市联想列表。
     *
     * @param searchWord
     * @param pageSize
     * @return
     * @throws TravelPiException
     */
    public static Iterator<Locality> getSuggestion(String searchWord, int pageSize) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class).filter("zhName", Pattern.compile("^" + searchWord));
        query.field("relPlanCnt").greaterThan(0);
        return query.retrievedFields(true, "zhName", "enName", "country", "level", "superAdm", "abroad")
                .limit(pageSize).iterator();
    }


    /**
     * 通过关键词对城市进行搜索。
     *
     * @param keyword  搜索关键词。
     * @param prefix   是否为前缀搜索？
     * @param page     分页偏移量。
     * @param pageSize 页面大小。
     */
    public static java.util.Iterator<Locality> searchLocalities(String keyword, ObjectId countryId,
                                                                int scope, boolean prefix,
                                                                int page, int pageSize) throws TravelPiException,PatternSyntaxException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);

        Query<Locality> query = ds.createQuery(Locality.class);
        if (keyword != null && !keyword.isEmpty())
            query.filter("zhName", Pattern.compile(prefix ? "^" + keyword : keyword));
        if (countryId != null)
            query.filter("country.id", countryId);
        switch (scope) {
            case 1:
                query.filter("abroad", false);
                break;
            case 2:
                query.filter("abroad", true);
                break;
            default:
        }

        return query.order("level").offset(page * pageSize).limit(pageSize).iterator();
    }

    /**
     * 获得城市信息。
     *
     * @param locId
     * @return
     */
    public static Locality getLocality(ObjectId locId) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        return ds.createQuery(Locality.class).field("_id").equal(locId).field("enabled").equal(Boolean.TRUE).get();
    }

    /**
     * 发现城市。
     *
     * @param showDetails 是否显示详情。
     * @param page        分页。
     * @param pageSize    页面大小。
     * @return
     */
    public static List<Locality> explore(boolean showDetails, int page, int pageSize) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);

        List<String> fields = new ArrayList<>();
        Collections.addAll(fields, "zhName", "ratings");
        if (showDetails)
            Collections.addAll(fields, "imageList", "tags", "desc");
        Query<Locality> query = ds.createQuery(Locality.class).field("level").equal(2)
                .field("imageList").notEqual(null)
                .field("relPlanCnt").greaterThan(0)
                .retrievedFields(true, fields.toArray(new String[]{""}))
                .offset(page * pageSize).limit(pageSize).order("-ratings.baiduIndex, -ratings.score");

        return query.asList();
    }

    /**
     * 根据名称搜索国家。
     *
     * @param keyword
     * @return
     * @throws TravelPiException
     */
    public static List<Country> searchCountryByName(String keyword, int page, int pageSize) throws TravelPiException {
        Query<Country> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO).createQuery(Country.class);
        query.or(
                query.criteria("zhName").equal(keyword),
                query.criteria("enName").equal(Pattern.compile("^" + keyword, Pattern.CASE_INSENSITIVE)),
                query.criteria("alias").equal(Pattern.compile("^" + keyword, Pattern.CASE_INSENSITIVE))
        );
        return query.order("-isHot").offset(page * pageSize).limit(pageSize).asList();
    }

    /**
     * 根据国家代码搜索国家。
     *
     * @param keyword
     * @return
     */
    public static List<Country> searchCountryByCode(String keyword, int page, int pageSize) throws TravelPiException {
        Query<Country> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO).createQuery(Country.class);
        query.or(
                query.criteria("code").equal(keyword),
                query.criteria("code3").equal(keyword)
        );
        return query.order("-isHot").offset(page * pageSize).limit(pageSize).asList();
    }

    /**
     * 根据地区搜索国家。
     *
     * @param keyword
     * @param page
     * @param pageSize
     * @return
     */
    public static List<Country> searchCountryByRegion(String keyword, int page, int pageSize) throws TravelPiException {
        Query<Country> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO).createQuery(Country.class);
        if (keyword != null && !keyword.isEmpty()) {
            query.or(
                    query.criteria("zhCont").equal(keyword),
                    query.criteria("enCont").equal(keyword),
                    query.criteria("zhRegion").equal(keyword),
                    query.criteria("enRegion").equal(keyword)
            );
        }
        query.field("enabled").equal(true);
        return query.order("-isHot").offset(page * pageSize).limit(pageSize).asList();
    }
}
