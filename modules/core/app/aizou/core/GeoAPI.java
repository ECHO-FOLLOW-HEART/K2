package aizou.core;

import com.lvxingpai.yunkai.UserInfo;
import exception.AizouException;
import models.AizouBaseEntity;
import database.MorphiaFactory;
import models.geo.Country;
import models.geo.Locality;
import models.misc.Track;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.Collection;
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
        Datastore ds = MorphiaFactory.datastore();
        Query<Country> query = ds.createQuery(Country.class).field("_id").equal(countryId);
        if (field != null && !field.isEmpty())
            query.retrievedFields(true, field.toArray(new String[field.size()]));

        return query.get();
    }

    public static Country countryDetails(String countryId) throws AizouException {
        return countryDetails(new ObjectId(countryId), null);
    }

    public static List<Country> getAllCountryList(List<String> field) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<Country> query = ds.createQuery(Country.class);
        if (field != null && !field.isEmpty())
            query.retrievedFields(true, field.toArray(new String[field.size()]));
        return query.asList();
    }

    public static List<Country> getAllCountryList() throws AizouException {
        return getAllCountryList(null);
    }

    // 后续通过配置做，目前只是写一个接口
    public static List<UserInfo> getExpertsByCountry(String countryId) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        // 通过国家ID找到所有的足迹
        Query<Track> query = ds.createQuery(Track.class);
        // 得到所有足迹后，找到userId
        // 根据userId找到达人信息
        List<UserInfo> experts = null;
        return experts;
    }

    /**
     * 获得城市详情。
     *
     * @param locId 城市ID。
     * @return 如果没有找到，返回null。
     * @throws exception.AizouException
     */
    public static Locality locDetails(ObjectId locId, List<String> fields) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<Locality> query = ds.createQuery(Locality.class).field("_id").equal(locId).field(AizouBaseEntity.FD_TAOZIENA).equal(true);
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
    public static java.util.Iterator<Locality> searchLocalities(String keyword, boolean prefix, ObjectId countryId,
                                                                int page, int pageSize) throws AizouException {
        return searchLocalities(keyword, prefix, countryId, page, pageSize, null);
    }

    public static java.util.Iterator<Locality> searchLocalities(String keyword, boolean prefix, ObjectId countryId,
                                                                int page, int pageSize, Collection<String> fields)
            throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<Locality> query = ds.createQuery(Locality.class);
        if (keyword != null && !keyword.isEmpty())
            query.field("alias").equal(Pattern.compile("^" + keyword));

        if (countryId != null)
            query.field(String.format("%s.id", Locality.fnCountry)).equal(countryId);
        query.field(AizouBaseEntity.FD_TAOZIENA).equal(true);

        if (fields != null && !fields.isEmpty())
            query.retrievedFields(true, fields.toArray(new String[fields.size()]));
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
        Query<Country> query = MorphiaFactory.datastore().createQuery(Country.class);
        if (!keyword.equals("")) {
            query.field("alias").equal(Pattern.compile("^" + keyword));
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
        Query<Country> query = MorphiaFactory.datastore().createQuery(Country.class);
        if (keywords != null) {
            List<CriteriaContainerImpl> criList = new ArrayList<>();
            for (String word : keywords)
                criList.add(query.criteria("alias").equal(Pattern.compile("^" + word)));
            query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        }
        query.field(AizouBaseEntity.FD_TAOZIENA).equal(true);
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

        Datastore ds = MorphiaFactory.datastore();
        Query<Locality> query = ds.createQuery(Locality.class);
        query.field("abroad").equal(abroad).field("enabled").equal(true);
        query.offset(page * pageSize).limit(pageSize);
        query.order("-hotness");
        return query.asList();
    }

    public static List<Locality> getDestinationsByCountry(ObjectId countryID, int page, int pageSize) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<Locality> query = ds.createQuery(Locality.class);
        query.field("country._id").equal(countryID).field(AizouBaseEntity.FD_TAOZIENA).equal(true);
        query.order("-hotness");
        query.offset(page * pageSize).limit(pageSize);
        query.retrievedFields(true, AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME, Locality.fnLocation, Locality.fnImages);
        return query.asList();
    }
}
